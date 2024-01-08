/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/06/26  V1.00.01    Alex      改寫黑名單規則改為TCB版本                                                                   *
 *  112/10/04  V1.00.02    Wilson    增加讀取凍結碼38                               *
 *  112/10/11  V1.00.03    Wilson    增加讀取同業強停(其他停用-E2)                     *
 *  112/10/12  V1.00.04    Wilson    增加讀取卡特指&所有凍結碼                                                                     *
 *  112/10/20  V1.00.05    Wilson    調整讀取毀損補發邏輯                                                                               *
 ******************************************************************************/
package Ich;
import com.Parm2sql;
import com.BaseBatch;

public class IchR001 extends BaseBatch {
   private String progname = "愛金卡黑名單批次處理  112/10/20 V1.00.05";
   private String hBalanceDate = "";
   //private String h_black_flag = "";
   private String hIchCardNo = "";
   private String hCurrentCode = "";
   private String hPSeqno = "";
   private String hCardNo = "";
   private String isMcodeCode = "";
   private String isPaymentRate = "";
   private double imMcodeAmt = 0;
   private String isBlockCond = "";
   private String isBlockReason = "";
   private String last90Date = "";
   String hBlackltFlag="", hBlackltSDate="", hBlackltEDate="";
   String fromType = "";
   int logCnt=0;

   private int tiAct = -1;
   private int tiAcct = -1;

   public static void main(String[] args) {
      IchR001 proc = new IchR001();
//   proc.debug = true;
      proc.mainProcess(args);
      proc.systemExit(0);
   }

   @Override
   protected void dataProcess(String[] args) throws Exception {
      dspProgram(progname);

      int liArg = args.length;
      if (liArg > 1) {
         printf("Usage : IchR001 [busi_date,batch_seq]");
         errExit(1);
      }

      dbConnect();

      // comr = new CommRoutine(getDBconnect(), getDBalias());
      if (liArg > 0) {
         setBusiDate(args[0]);
         callBatchSeqno(args[liArg - 1]);
      }
      callBatch(0, 0, 0);
      
      //--90天
      last90Date = commDate.dateAdd(hBusiDate, 0, 0, -90);
      this.showLogMessage("I", "", "營業日 = ["+hBusiDate+"] 往前90日 = ["+last90Date+"] ");
 
      //-人工指定名單-
      selectIchBlackList();
      
      //--問題交易
      selectA04B();

      //-批次名單-
      selectIchCard();      
      
      printf("Insert 黑名單筆數=[%s]",logCnt);

      sqlCommit(1);
      endProgram();
   }
//---------------------------------------------------------------------------
   void selectIchBlackList() throws Exception {

      String lsBlackReason = "";

      sqlCmd = " select "
              + " A.ich_card_no ,"
              + " A.black_flag ,"
              + " C.current_code ,"
              + " B.balance_date, B.blacklt_flag, B.blacklt_s_date, B.blacklt_e_date "
              + " from ich_card B join ich_black_list A on B.ich_card_no=A.ich_card_no "
              +" join crd_card C on C.card_no=B.card_no"
              + " where A.black_flag in ('1','2','4') "
              + " and ? between decode(A.send_date_s,'','0',A.send_date_s) and decode(A.send_date_e,'','99999999',A.send_date_e) "
              + " and B.current_code ='0' "
              + " and ? between B.new_beg_date and B.new_end_date"  //--未屆期
              + " and B.return_date = '' "  //--未退卡
              + " and B.lock_date = '' "    //--未鎖卡
              + " and A.apr_date <> '' "
              + " order by A.black_flag"
      ;

      ppp(1,hBusiDate);
      ppp(hBusiDate);

      openCursor();

      while(fetchTable()){
    	  initData();
    	  totalCnt++;
    	  hBalanceDate = colSs("balance_date");
    	  String hBlackFlag = colSs("black_flag");
    	  hIchCardNo = colSs("ich_card_no");
    	  hCurrentCode = colSs("current_code");
    	  hBlackltFlag =colSs("blacklt_flag");
    	  hBlackltSDate =colSs("blacklt_s_date");
    	  hBlackltEDate =colSs("blacklt_e_date");    	  

    	  //--已餘轉不報送
    	  if(!empty(hBalanceDate) && (eqIgno(hBlackFlag,"1")||eqIgno(hBlackFlag,"3"))) {
    		  if (eq(hBlackltFlag,"Y") && empty(hBlackltEDate)) {
    			  hBlackltEDate =sysDate;
    			  updateIchCard(hIchCardNo);
    		  }
    		  continue;
    	  }
    	  //--黑名單強制報送
    	  lsBlackReason = "1"+hBlackFlag;
    	  insertIchBlackLog("1",lsBlackReason);
    	  if (!eq(hBlackltFlag,"Y") || noEmpty(hBlackltEDate)) {
    		  hBlackltFlag ="Y";
    		  hBlackltSDate =sysDate;
    		  hBlackltEDate ="";
    		  updateIchCard(hIchCardNo);
    	  }
      }
      closeCursor();

   }

   //---------------------------------------------------------------------------
   int tiIchCardU=-1;
   void updateIchCard(String aIchCrdno) throws Exception {
      if (tiIchCardU<0) {
         sqlCmd ="update ich_card set"+
                 " blacklt_flag =?,"+
                 " blacklt_s_date =?,"+
                 " blacklt_e_date =?,"+
                 modxxxSet()+
                 " where ich_card_no =?";
         tiIchCardU =ppStmtCrt("ich_card-U","");
      }

      ppp(1, hBlackltFlag);
      ppp(hBlackltSDate);
      ppp(hBlackltEDate);
      ppp(aIchCrdno);
      sqlExec(tiIchCardU);
      if (sqlNrow <=0) {
         sqlerr("update ich_card error kk=[%s]", aIchCrdno);
         errExit(1);
      }
   }
   //---------------------------------------------------------------------------
   com.Parm2sql ttBklog=null;
   void insertIchBlackLog(String aFromType, String aReason) throws Exception{
      //ddd("log: type[%s],reason[%s]",a_from_type,a_reason);

      if (ttBklog==null) {
         ttBklog = new Parm2sql();
         ttBklog.insert("ich_black_log");
      }
      ttBklog.aaa("ich_card_no", hIchCardNo);
      ttBklog.aaa("current_code", hCurrentCode);
      ttBklog.aaa("from_mark", aFromType);
      ttBklog.aaa("black_reason", aReason);
      ttBklog.aaa("proc_flag","N");
      ttBklog.aaa("order_seqno",0);
      ttBklog.aaaYmd("crt_date");
      ttBklog.aaaTime("crt_time");
      ttBklog.aaaDtime("mod_time");
      ttBklog.aaa("mod_pgm",hModPgm);

      if (ttBklog.ti <=0) {
         ttBklog.ti =ppStmtCrt("tt_bklog-A",ttBklog.getConvSQL());
      }

      sqlExec(ttBklog.ti,ttBklog.getConvParm());
      if (sqlNrow <= 0) {
         sqlerr("insert_ich_black_log error");
         errExit(1);
      }

      logCnt++;

   }

   //---------------------------------------------------------------------------   
   void selectIchCard() throws Exception {
	   
	          //停用、凍結碼38-排序3
	   sqlCmd = " select A.card_no , A.ich_card_no , C.acno_p_seqno , A.blacklt_flag , "
	   		  + " A.blacklt_s_date , A.blacklt_e_date , B.black_flag , B.send_date_s , B.send_date_e , '3' as from_type , C.current_code "
	   		  + " from ich_card A join crd_card C on C.card_no = A.card_no "
	   		  + " left join ich_black_list B on B.ich_card_no = A.ich_card_no "
	   		  + " left join cca_card_acct D on C.acno_p_seqno = D.acno_p_seqno "
	   		  + " where 1=1 "
	   		  + " and ? between A.new_beg_date and A.new_end_date "
	   		  + " and A.return_date = '' and A.balance_date = '' and A.lock_date = '' "
	   		  + " and ( (C.current_code not in ('0','2') and C.oppost_date >= ? ) "
//	   		  + "       and C.oppost_reason in ('A2','J2','H1','Z2','U1','B5','B6','AP','T1','F1','E4','B2','B3','B1','B4','AX','EB','C2','ED','EE','M1','N1','AK','MS','MF','O1','O2','O3','S0','J1','E2')) "
	   		  + "  or (((D.block_reason1 = '38') or (D.block_reason2 = '38') or (D.block_reason3 = '38') or (D.block_reason4 = '38') or (D.block_reason5 = '38')) and D.block_date >= ?) ) "
	   		  + " union "
	   		 //逾期-排序4	   		  
	   		  + " select A.card_no , A.ich_card_no , C.acno_p_seqno , A.blacklt_flag , "
	   		  + " A.blacklt_s_date , A.blacklt_e_date , B.black_flag , B.send_date_s , B.send_date_e , '4' as from_type , C.current_code "
	   		  + " from ich_card A join crd_card C on C.card_no = A.card_no "
	   		  + " join act_acno D on C.acno_p_seqno = D.acno_p_seqno "
	   		  + " join act_acct E on C.p_seqno = E.p_seqno "
	   		  + " left join ich_black_list B on B.ich_card_no = A.ich_card_no "
	   		  + " where 1=1 "
	   		  + " and ? between A.new_beg_date and A.new_end_date "
	   		  + " and A.return_date = '' and A.balance_date = '' and A.lock_date = '' "
       	      + " and D.INT_RATE_MCODE >= 1 "
	   		  + " union "
	   		  //凍結碼-排序5
	   		  + " select A.card_no , A.ich_card_no , C.acno_p_seqno , A.blacklt_flag , "
	   		  + " A.blacklt_s_date , A.blacklt_e_date , B.black_flag , B.send_date_s , B.send_date_e , '5' as from_type , C.current_code "
	   		  + " from ich_card A join crd_card C on C.card_no = A.card_no "
	   		  + " join cca_card_acct D on C.acno_p_seqno = D.acno_p_seqno "
	   		  + " left join ich_black_list B on B.ich_card_no = A.ich_card_no "
	   		  + " where 1=1 "
	   		  + " and ? between A.new_beg_date and A.new_end_date "
	   		  + " and A.return_date = '' and A.balance_date = '' and A.lock_date = '' "
	   		  + " and ( (D.block_reason1 || D.block_reason2 || D.block_reason3 || D.block_reason4 || D.block_reason5 <> '') "
	   		  + "    and (D.block_reason1 <> '38') and (D.block_reason2 <> '38') and (D.block_reason3 <> '38') and (D.block_reason4 <> '38') and (D.block_reason5 <> '38') ) "	   		  
	   		  + " union "
	   		  //卡特指-排序6
	   		  + " select A.card_no , A.ich_card_no , C.acno_p_seqno , A.blacklt_flag , "
	   		  + " A.blacklt_s_date , A.blacklt_e_date , B.black_flag , B.send_date_s , B.send_date_e , '6' as from_type , C.current_code "
	   		  + " from ich_card A join crd_card C on C.card_no = A.card_no "
	   		  + " join cca_card_base D on C.card_no = D.card_no "
	   		  + " left join ich_black_list B on B.ich_card_no = A.ich_card_no "
	   		  + " where 1=1 "
	   		  + " and ? between A.new_beg_date and A.new_end_date "
	   		  + " and A.return_date = '' and A.balance_date = '' and A.lock_date = '' "
	   		  + " and D.spec_status <> '' "
	   		  + " union "
	   		  //掛失停用、毀損補發-排序7
	   		  + " select A.card_no , A.ich_card_no , C.acno_p_seqno , A.blacklt_flag , "
	   		  + " A.blacklt_s_date , A.blacklt_e_date , B.black_flag , B.send_date_s , B.send_date_e , '7' as from_type , C.current_code "
	   		  + " from ich_card A join crd_card C on C.card_no = A.card_no "
	   		  + " left join ich_black_list B on B.ich_card_no = A.ich_card_no "
	   		  + " where 1=1 "
	   		  + " and ? between A.new_beg_date and A.new_end_date "
	   		  + " and A.return_date = '' and A.balance_date = '' and A.lock_date = '' "
	   		  + " and ( (C.current_code ='2' and C.oppost_date >= ?) or (A.current_code ='6') or (A.current_code ='4') ) "
	   		  + " order by 1 Asc "
	   		  ;
	   
	   setString(1,hBusiDate);
	   setString(2,last90Date);
	   setString(3,last90Date);
	   setString(4,hBusiDate);
	   setString(5,hBusiDate);
	   setString(6,hBusiDate);
	   setString(7,hBusiDate);
	   setString(8,last90Date);
	   
	   openCursor();
	   
	   while(fetchTable()) {		   
	       totalCnt++;
	       initData();
	       hCardNo = colSs("card_no");
	       hPSeqno = colSs("acno_p_seqno");
	       hIchCardNo =colSs("ich_card_no");
	       hBlackltFlag =colSs("blacklt_flag");
	       hBlackltSDate =colSs("blacklt_s_date");
	       hBlackltEDate =colSs("blacklt_e_date");
	       String lsBlack =colSs("black_flag");
	       String lsDate1 =colSs("send_date_s");
	       String lsDate2 =colNvl("send_date_e","29991231");
	       fromType = colSs("from_type");
	       hCurrentCode = colSs("current_code");
	       //-排除人工指定黑名單-
	       if (!empty(lsBlack)){	    	   
	    	   if (!commString.between(hBusiDate,lsDate1,lsDate2)) {
	    		   lsBlack ="";
	    	   }
	       }
	       //-人工報送-
	       if (commString.ssIn(lsBlack,",1,2,4")) {
	    	   continue;
	       }
	       else if (eq(lsBlack,"3")) {
	    	   //-人工不報送-
	    	   if (eq(hBlackltFlag,"Y") && empty(hBlackltEDate)) {
	    		   hBlackltEDate = sysDate;
	    		   updateIchCard(hIchCardNo);
	    	   }
	    	   continue;
	       }
	       
	       hBlackltFlag ="Y";
	       hBlackltSDate =sysDate;
	       hBlackltEDate ="";
	       
	       insertIchBlackLog(fromType,fromType+"1");
	       updateIchCard(hIchCardNo);
	   }
	   closeCursor();
   }
   
   void selectA04B() throws Exception {
	   
	   sqlCmd = " select A.card_no , B.ich_card_no , C.acno_p_seqno , A.blacklt_flag , " 
			  + " A.blacklt_s_date , A.blacklt_e_date , D.black_flag , D.send_date_s , D.send_date_e , '2' as from_type , C.current_code "
			  + " from ich_card A join ich_a04b_exception B on A.ich_card_no = B.ich_card_no "
			  + " join crd_card C on A.card_no = C.card_no "
			  + " left join ich_black_list D on A.ich_card_no = D.ich_card_no "
			  + " where A.current_code ='0' "
			  + " and ? between A.new_beg_date and A.new_end_date "
			  + " and A.return_date = '' "
			  + " and A.balance_date = '' "
			  + " and A.lock_date = '' "
			  + " and B.sys_date >= ? "
			  ;
	   
	   setString(1,hBusiDate);
	   setString(2,last90Date);
	   
	   openCursor();
	   
	   while(fetchTable()) {
	       totalCnt++;
	       initData();
	       hCardNo = colSs("card_no");
	       hPSeqno = colSs("acno_p_seqno");
	       hIchCardNo =colSs("ich_card_no");
	       hBlackltFlag =colSs("blacklt_flag");
	       hBlackltSDate =colSs("blacklt_s_date");
	       hBlackltEDate =colSs("blacklt_e_date");
	       String lsBlack =colSs("black_flag");
	       String lsDate1 =colSs("send_date_s");
	       String lsDate2 =colNvl("send_date_e","29991231");
	       fromType = colSs("from_type");
	       hCurrentCode = colSs("current_code");
	       
	       if (!empty(lsBlack)){	    	   
	    	   if (!commString.between(hBusiDate,lsDate1,lsDate2)) {
	    		   lsBlack ="";
	    	   }
	       }
	       //-人工報送-
	       if (commString.ssIn(lsBlack,",1,2,4")) {
	    	   continue;
	       }
	       else if (eq(lsBlack,"3")) {
	    	   //-人工不報送-
	    	   if (eq(hBlackltFlag,"Y") && empty(hBlackltEDate)) {
	    		   hBlackltEDate = sysDate;
	    		   updateIchCard(hIchCardNo);
	    	   }
	    	   continue;
	       }
	       
	       hBlackltFlag ="Y";
	       hBlackltSDate =sysDate;
	       hBlackltEDate ="";
	       
	       insertIchBlackLog(fromType,fromType+"1");
	       updateIchCard(hIchCardNo);	       
	   }
	   closeCursor();
   }
   
   //---------------------------------------------------------------------------   
   void initData() {	  
	   hIchCardNo = "";
	   hCurrentCode = "";
	   hPSeqno = "";
	   hCardNo = "";
	   isMcodeCode = "";
	   isPaymentRate = "";
	   imMcodeAmt = 0;
	   isBlockCond = "";
	   isBlockReason = "";
	   hBlackltFlag="";
	   hBlackltSDate="";
	   hBlackltEDate="";
	   fromType = "";
   }
   
}

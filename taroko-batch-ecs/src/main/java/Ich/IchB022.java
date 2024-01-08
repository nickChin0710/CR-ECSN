/** 愛金卡拒絕代行名單產生處理 V.19-0311
 * 2020-0617 V1.00.00   JH    modify
 * 2020-0615 V1.00.00   JH    ++戶特指
 * *  109/11/20  V1.00.02  yanghan       修改了變量名稱和方法名稱  
 *  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
 * */

package Ich;

import com.BaseBatch;

public class IchB022 extends BaseBatch {
private String progname = "愛金卡拒絕代行名單產生處理 109/12/24   V1.00.03";
private String hIchCardNo = "";
private String hCurrentCode = "";
private String hCardNo = "";
private String isBlockCond = "";
private String isBlockReason = "";
private String isSpecCond = "";
private String isSpecStatus = "";
private String last90Date = "";
private int tiAcct = -1;
private int tiRefuse=-1;

//=****************************************************************************
public static void main(String[] args) {
   IchB022 proc = new IchB022();
   //proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : IchB022 [batch_seq]");
      okExit(1);
   }

   dbConnect();

   // comr = new CommRoutine(getDBconnect(), getDBalias());
   if (liArg > 0) {
	   setBusiDate(args[0]);
	   callBatchSeqno(args[liArg - 1]);
   }
   callBatch(0, 0, 0);
   last90Date = commDate.dateAdd(hBusiDate, 0, 0, -90);
   //-停卡拒絕代行-
   selectIchCard5();
   selectIch00Parm();
   //-取消拒絕代行-
   selectIchCardQ();
   //-批次拒絕名單-
   selectIchCardR();

   sqlCommit(1);
   endProgram();

}

//----------------------------------------------------------------------------
void selectIchCard5() throws Exception {

   sqlCmd = " select "
         + " A.ich_card_no, A.current_code , A.card_no "
         + " from ich_card A join crd_card B on A.card_no = B.card_no "
         + " where B.current_code <> '0' "
         + " and B.oppost_date >= ? "
         + " and A.return_date ='' "
         + " and A.lock_date ='' "
         + " and A.refuse_type <>'5' "
         ;

   setString(1,last90Date);
   openCursor();      
   
   while(fetchTable()){
      hIchCardNo = colSs("ich_card_no");
      hCurrentCode = colSs("current_code");
      hCardNo = colSs("card_no");
      insertIchRefuseLog("3",hCurrentCode,"5");
   }

   closeCursor();
}

//---------------------------------------------------------------------------
com.Parm2sql ttRefuse=null;
void insertIchRefuseLog(String aDrom, String aCode , String aRefuse) throws Exception{
   if (ttRefuse==null) {
      ttRefuse = new com.Parm2sql();
      ttRefuse.insert("ich_refuse_log");
   }
   ttRefuse.aaaYmd("crt_date");
   ttRefuse.aaaTime("crt_time");
   ttRefuse.aaa("card_no",hCardNo);
   ttRefuse.aaa("ich_card_no", hIchCardNo);
   ttRefuse.aaa("from_type", aDrom+aCode);
   ttRefuse.aaa("secu_code", aCode);
   ttRefuse.aaa("risk_remark", "拒絕參數");
   ttRefuse.aaa("refuse_type", aRefuse);
   ttRefuse.aaa("crt_user", hModUser);
   ttRefuse.aaaModxxx(hModUser,hModPgm);

   if (ttRefuse.ti <=0) {
      ttRefuse.ti =ppStmtCrt("tt_refuse_log-A",ttRefuse.getConvSQL());
   }

   sqlExec(ttRefuse.ti,ttRefuse.getConvParm());
   if (sqlNrow <= 0) {
      sqlerr("insert_ich_black_log error");
      errExit(1);
   }
   
   totalCnt++;
}

//---------------------------------------------------------------------------
void selectIch00Parm() throws Exception {

   String sql1 = " select block_cond, block_reason , spec_cond , spec_status "
         + " from ich_00_parm "
         + " where parm_type ='ICHM0060' "
         + " and apr_date <> '' "
         + " fetch first 1 rows only "
         ;

   sqlSelect(sql1);

   if(sqlNrow<=0)	return ;

   isBlockCond = colSs("block_cond");
   isBlockReason = colSs("block_reason");
   isSpecCond = colSs("spec_cond");
   isSpecStatus = colSs("spec_status");
}

//---------------------------------------------------------------------------
void selectIchCardQ() throws Exception {
   //-取消拒絕代行: 是否取消[R >> Q]-
   sqlCmd = " select "
         + " A.card_no, A.ich_card_no "
         + " from ich_card A "
         + " where A.current_code ='0' "	//--有效卡
         + " and ? between A.new_beg_date and A.new_end_date "
         + " and decode(A.return_date,'','N',A.return_date) ='N' "	//--未退卡
         + " and decode(A.balance_date,'','N',A.balance_date) ='N' "	//--未餘轉
         + " and A.lock_date = '' "	//--未鎖卡
         + " and A.refuse_type ='R' "	//--拒絕代行
   ;

   ppp(1,hBusiDate);

   openCursor();

   while(fetchTable()){
      hCardNo = colSs("card_no");
      hIchCardNo = colSs("ich_card_no");
      //-==0.取消-
      if(checkCondParm(hCardNo) !=0)	continue;
      if (checkRefuseLog(hIchCardNo) !=1) {
         //-1.批次拒絕代行-
		   printf("-->no batch-parm reject[%s]",hIchCardNo);
         continue;
      }
      insertIchRefuseLog("2","0","Q");
   }
   closeCursor();
}
int checkRefuseLog(String aIchCardno) throws Exception {
   //return: 1.批次拒絕--
   if (tiRefuse <=0) {
      sqlCmd ="select crt_date, from_type, refuse_type"
         +" from ich_refuse_log"
         +" where ich_card_no =?"
         +" order by crt_date desc, crt_time desc"
         +commSqlStr.rownum(1)
      ;
      tiRefuse =ppStmtCrt("refuse_log-S","");
   }
   ppp(1,aIchCardno);
   //ddd_sql(ti_refuse);
   sqlSelect(tiRefuse);
   if (sqlNrow >0) {
      String lsType =commString.left(colSs("from_type"),1);
      //-2.批次,R.拒絕-
      if (eq(lsType,"2") && colEq("refuse_type","R")) {
         return 1;  //批次拒絕代行
      }
   }

   return 0;
}
//---------------------------------------------------------------------------

int checkCondParm(String aCardNo) throws Exception{
   //--戶凍結, 卡持指; 0.無符合參數, 1.符合參數

   if (tiAcct <= 0) {
      sqlCmd = " select "
            + " C.block_reason1 as block_reason1 , "
            + " C.block_reason2 as block_reason2 , "
            + " C.block_reason3 as block_reason3 , "
            + " C.block_reason4 as block_reason4 , "
            + " C.block_reason5 as block_reason5 , "
            + " C.block_reason1||C.block_reason2||C.block_reason3||C.block_reason4||C.block_reason5 as wk_block_reason , "
            + " uf_spec_status(C.spec_status,C.spec_del_date) as acct_spec_status , "
            + " uf_spec_status(D.spec_status,D.spec_del_date) as card_spec_status , "
            + " C.block_date , C.spec_date as acct_spec_date , D.spec_date as card_spec_date "
            + " from act_acno A "
            + " join crd_card B on A.p_seqno=B.p_seqno " //A.acct_p_seqno=B.acct_p_seqno
            + " left join cca_card_acct C on A.acno_p_seqno = C.acno_p_seqno  and decode(C.debit_flag,'','N',C.debit_flag) = 'N'  "
            + " left join cca_card_base D on D.card_no = B.card_no "
            + " where B.card_no = ? "
//            + " and A.debit_flag <>'Y' "
//            + "  "
      ;
      tiAcct = ppStmtCrt("ti_acct", "");
   }

   ppp(1, aCardNo);
   sqlSelect(tiAcct);
   if(sqlNrow<=0)	return 0;

   //-無凍結,特指-
   if(empty(colSs("wk_block_reason")) && empty(colSs("acct_spec_status")) && empty(colSs("card_spec_status"))) {
      return 0;
   }

   //-負向表列-
   if(eqIgno(isBlockCond,"Y")){
      for (int ii=1; ii<=5; ii++) {
         String lsBlock=colSs("block_reason"+ii);
         if (!empty(lsBlock) && !strIN(lsBlock,isBlockReason) && colSs("block_date").compareTo(last90Date) >= 0) {
            return 1;
         }
      }
   }	else if(eqIgno(isBlockCond,"Y") == false && colEmpty("wk_block_reason") == false && colSs("block_date").compareTo(last90Date) >= 0)	{
	   return 1;
   }

   if(eqIgno(isSpecCond,"Y")){
      String lsSpec =colSs("acct_spec_status");
      if (!empty(lsSpec)) { // && col_nvl("acct_del_date","29991231").compareTo(this.sysDate)>0) {
         if (!strIN(lsSpec,isSpecStatus) && colSs("acct_spec_date").compareTo(last90Date) >= 0)
            return 2;
      }

      lsSpec =colSs("card_spec_status");
      if (!empty(lsSpec)) { // && col_nvl("card_del_date","29991231").compareTo(this.sysDate)>0) {
		//ddd("2.card_no[%s], spec[%s], cond[%s]",a_card_no,ls_spec,is_spec_status);
         if (!strIN(lsSpec,isSpecStatus) && colSs("card_spec_date").compareTo(last90Date) >= 0)
            return 2;
      }
   }	else if(eqIgno(isSpecCond,"Y") == false && (colEmpty("acct_spec_status") == false || colEmpty("card_spec_status") == false)) {	   
	   if(colEmpty("acct_spec_status") == false && colSs("acct_spec_date").compareTo(last90Date) >= 0)		   
		   return 2;
	   if(colEmpty("card_spec_date") == false && colSs("card_spec_date").compareTo(last90Date) >= 0)		   
		   return 2;
   }

   return 0;
}
//---------------------------------------------------------------------------
void selectIchCardR() throws Exception {
   //-正常,取消: 是否拒絕['',Q >>R]-
   sqlCmd = " select "
         + " A.card_no, A.ich_card_no "
         + " from ich_card A "
         + " where A.current_code ='0' "	//--有效卡
         + " and ? between A.new_beg_date and A.new_end_date "
         + " and A.return_date = '' "	//--未退卡
         + " and A.balance_date = '' "	//--未餘轉
         + " and A.lock_date = '' "	//--未鎖卡
         + " and nvl(A.refuse_type,'') in ('','Q') "	//--正常,取消拒絕代行         
         ;

   ppp(1,hBusiDate);
   openCursor();

   while(fetchTable()){
      hCardNo = colSs("card_no");
      hIchCardNo = colSs("ich_card_no");
            
      int liCond =checkCondParm(hCardNo);
      if(liCond==0)	continue;
      insertIchRefuseLog("2",""+liCond,"R");
   }
   closeCursor();
}
}

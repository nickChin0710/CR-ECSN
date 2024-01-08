package Cms;
/** ref.CmsC011
 * 2023-0705 V1.00.02   JH    ++dsDetl.loadKeyData()
 * 2023-0704 V1.00.02   JH    naming rule
 * 2023-0605 V1.00.01   JH    item_no=10,11
 * 2023-0530 V1.00.00   JH    initial
 * */

import com.DataSet;
import com.Parm2sql;
import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class CmsC009 extends com.BaseBatch {
private final String PROGNAME = "卡人權益可使用統計處理-卡人  2023-0705 V1.00.02";
CmsRightParm ooParm=null;
//--
com.DataSet dsParm=new DataSet();
com.DataSet dsDetl=new DataSet();

//=*****************************************************************************
public static void main(String[] args) {
   CmsC009 proc = new CmsC009();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CmsC009 [busi_date(08), callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate = Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
      callBatchSeqno(args[0]);
   }
   if (args.length == 2) {
      callBatchSeqno(args[1]);
   }

   dbConnect();
   callBatch(0, 0, 0);

   ooParm = new CmsRightParm(getDBconnect(), getDBalias(),hBusiDate);
   ooParm.setYymm();
   printf("-- 資料處理年月[%s], busiDate[%s], acctMonth[%s]"
           , ooParm.isProcYymm, hBusiDate, ooParm.isAcctYymm);

   deleteCmsRightCal();
   sqlCommit();

   selectCmsRightParm();

   sqlCommit();
   endProgram();
}
//=================
void selectCmsRightParm() throws Exception {
   sqlCmd ="select * from cms_right_parm"+
           " where apr_flag='Y' and active_status='Y'"+
           " and ? between proj_date_s and decode(proj_date_e,'','20991231',proj_date_e)"+
           " and card_hldr_flag ='2'"+  //卡友
           " and item_no in ('10','11')";
   //TTT--
//   sqlCmd +=" and proj_code ='DXC07001'";
   sqlCmd +=" order by item_no, proj_code";

   ppp(1, hBusiDate);
   Object[] parms =getSqlParm();
   sqlQuery(dsParm,"",parms);
   int llNrow =dsParm.rowCount();
   if (llNrow <=0) {
      printf("無有效專案可處理");
      return;
   }

   sqlCmd = " select A.proj_code||'-'||A.item_no||'-'||A.data_type as kk_data"
           +", A.data_code, A.data_code2, A.data_code3"
           +" from cms_right_parm_detl A join cms_right_parm B"+
           " on A.proj_code=B.proj_code and A.item_no=B.item_no"
           +" where A.apr_flag ='Y' and A.table_id = 'RIGHT'"
           +" and B.apr_flag='Y' and B.active_status ='Y' and B.card_hldr_flag='2'"
           +" and B.item_no in ('10','11')"
           +" AND ? between B.proj_date_s and decode(B.proj_date_e,'','20991231',B.proj_date_e)"
           +" order by 1"
   ;
   this.sqlQuery(dsDetl, sqlCmd,new Object[]{hBusiDate});
   dsDetl.loadKeyData_dupl("kk_data");

   ooParm.setDsParm(dsParm);
   ooParm.setDsDetl(dsDetl);

   for (int ll = 0; ll <llNrow ; ll++) {
      dsParm.listCurr(ll);
      String lsItemNo =dsParm.colSs("item_no");
      String ls_projCode=dsParm.colSs("proj_code");
      printf("--process item_no[%s], proj_code[%s]...", lsItemNo, ls_projCode);
      if (commString.ssIn(lsItemNo,",10,11,")) {
         selectCrd_card_pp();
      }
      else {
         selectCrdCard();
      }
   }

}
//===========================
void selectCrdCard() throws Exception {
   String lsProjCode=dsParm.colSs("proj_code");
   String lsItemNo=dsParm.colSs("item_no");
   boolean condAcctType =dsParm.colEq("acct_type_flag","Y");
   boolean condGroupCard =dsParm.colEq("group_card_flag","Y");
   if (!condAcctType && !condGroupCard) {
      printf(" 專案代碼[%s] 未指定帳戶類別, 卡種+團代", lsProjCode);
      return;
   }

   sqlCmd ="select A.card_no, A.major_card_no, A.id_p_seqno, A.major_id_p_seqno"+
           ", A.group_code, A.card_type"+
           ", A.issue_date"+
           " FROM crd_card A"+
           " WHERE A.current_code=?"
   ;
   //TTTT--
//   sqlCmd +=" and A.card_no='4003538500507890'";

   ppp(1, "0");
   if (condAcctType) {
      sqlCmd += " AND A.acct_type IN (SELECT data_code FROM cms_right_parm_detl" +
              " WHERE apr_flag='Y' AND data_type='01' AND proj_code=? and item_no=?)";
      ppp(lsProjCode);
      ppp(lsItemNo);
   }
   if (condGroupCard) {
      sqlCmd += " AND EXISTS (SELECT 1 FROM cms_right_parm_detl" +
              " WHERE apr_flag='Y' AND data_type='02' AND proj_code=? and item_no=?" +
              " AND A.group_code like data_code||'%'" +
              " AND A.card_type like data_code2||'%'" +
              " )"
      ;
      ppp(lsProjCode);
      ppp(lsItemNo);
   }
   sqlCmd +=" order by A.card_no";

   int ll_totCnt=0;
   openCursor();
   while(fetchTable()) {
      ll_totCnt++;
      ooParm.initHh();
      String ls_cardNo =colSs("card_no");
      ooParm.selectCrdCard(ls_cardNo);
      if (empty(ooParm.hhCardNo)) continue;
      if (ooParm.checkProjCode(dsParm.getCurrRow()) !=0) continue;

      //-無免費次數-
      if (ooParm.hhFreeCnt <=0) continue;

      //--消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
      if (ooParm.hhCurrTotAmt <=0) {
         String ls_consumType =dsParm.colNvl("consume_type","0");
         String ls_itType =dsParm.colNvl("it_1_type","1");
         ooParm.selectBilBillCur(ooParm.isAcctYymm,ooParm.isAcctYymm,0,ls_itType,ls_consumType);
      }

      updateCmsRightCal();
   }
   closeCursor();
   printf(" proj_code[%s], item_no[%s], totCnt[%s]", lsProjCode, lsItemNo, ll_totCnt);
}
//=======================
void selectCrd_card_pp() throws Exception {
   String lsProjCode=dsParm.colSs("proj_code");
   String lsItemNo=dsParm.colSs("item_no");
   boolean condAcctType =dsParm.colEq("acct_type_flag","Y");
   boolean condGroupCard =dsParm.colEq("group_card_flag","Y");
   if (!condAcctType && !condGroupCard) {
      printf(" 專案代碼[%s] 未指定帳戶類別, 卡種+團代", lsProjCode);
      return;
   }

   int pp=1;
   sqlCmd ="select A.card_no, A.major_card_no, A.id_p_seqno, A.major_id_p_seqno"+
           ", A.group_code, A.card_type"+
           ", A.issue_date"+
           " FROM crd_card A join crd_card_pp B"+
           "   on A.card_no=B.card_no"+
           " WHERE A.current_code='0'";
   if (condAcctType) {
      sqlCmd +=" AND A.acct_type IN (SELECT data_code FROM cms_right_parm_detl"+
              " WHERE apr_flag='Y' AND data_type='01' AND proj_code=? and item_no=?)";
      ppp(pp++,lsProjCode);
      ppp(pp++, lsItemNo);
   }
   if (condGroupCard) {
      sqlCmd +=" AND EXISTS (SELECT 1 FROM cms_right_parm_detl"+
              " WHERE apr_flag='Y' AND data_type='02' AND proj_code=? and item_no=?"+
              " AND A.group_code like data_code||'%'"+
              " AND A.card_type like data_code2||'%'"+
              " )"
      ;
      ppp(pp++, lsProjCode);
      ppp(pp++, lsItemNo);
   }
   sqlCmd +=" order by A.card_no";

   int ll_totCnt=0;
   openCursor();
   while(fetchTable()) {
      ll_totCnt++;
      totalCnt++;

      ooParm.initHh();
      String ls_cardNo =colSs("card_no");
      ooParm.selectCrdCard(ls_cardNo);
      if (empty(ooParm.hhCardNo)) continue;
      if (ooParm.checkProjCode(dsParm.getCurrRow()) !=0) continue;

      //-無免費次數-
      if (ooParm.hhFreeCnt <=0) continue;

      //--消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
      if (ooParm.hhCurrTotAmt <=0) {
         String ls_consumType =dsParm.colNvl("consume_type","0");
         String ls_itType =dsParm.colNvl("it_1_type","1");
         ooParm.selectBilBillCur(ooParm.isAcctYymm,ooParm.isAcctYymm,0,ls_itType,ls_consumType);
      }

      updateCmsRightCal();
   }
   closeCursor();
   printf(" PPCard:proj_code[%s], item_no[%s], totCnt[%s]", lsProjCode, lsItemNo, ll_totCnt);
}
//==========================
void deleteCmsRightCal() throws Exception {
   sqlCmd ="delete cms_right_cal"+
           " where use_cnt =0"+
           " and curr_year =?"+
           " and free_type in ('1','2','3','4','6')"+
           " and data_from ='2'"+
           " and item_no in ('10','11')"
           ;

   ppp(1, ooParm.isProcYymm);
   sqlExec(sqlCmd);
   printf(" -- delete cms_right_cal row[%s]", sqlNrow);
}
//---------
com.Parm2sql ttUcal=null;
void updateCmsRightCal() throws Exception {
   if (ttUcal ==null) {
      ttUcal = new Parm2sql();
      ttUcal.update("cms_right_cal");
   }
   ttUcal.aaa("free_cnt",ooParm.hhFreeCnt);
   ttUcal.aaa("free_per_amt",ooParm.hhFreePerAmt);
   ttUcal.aaa("curr_proj_amt",ooParm.hhCurrProjAmt);
   ttUcal.aaa("curr_year_consume",ooParm.hhCurrYearConsume);
   ttUcal.aaa("free_cal_date",sysDate);
   ttUcal.aaaModxxx(hModUser,hModPgm);
   ttUcal.aaaWhere(" where id_p_seqno =?",ooParm.hhIdPseqno);
   ttUcal.aaaWhere(" and curr_year =?", ooParm.isProcYymm);
   ttUcal.aaaWhere(" and card_no =?", ooParm.hhCardNo);
   ttUcal.aaaWhere(" and item_no =?", ooParm.hhItemNo);
   ttUcal.aaaWhere(" and proj_code =?", ooParm.hhProjCode);
   ttUcal.aaaWhere(" and free_type =?", ooParm.hhFreeType);
   if (ttUcal.ti <=0) {
      ttUcal.ti =ppStmtCrt("tt-U-cal",ttUcal.getSql());
   }

//   ddd("cal2="+ttUcal.get_sql(),ttUcal.get_convParm(false));
   sqlExec(ttUcal.ti,ttUcal.getParms());
   if(sqlNrow <0){
      errmsg("update cms_right_cal error ");
      errExit(rc);
   }

   if (sqlNrow ==0) {
      insertCmsRightCal();
   }
}
//-----
com.Parm2sql ttAcal=null;
void insertCmsRightCal() throws Exception {
   if (ttAcal ==null) {
      ttAcal = new Parm2sql();
      ttAcal.insert("cms_right_cal");
   }
   ttAcal.aaa("id_p_seqno", ooParm.hhIdPseqno);          	//-VARCHAR (10,0)  卡人流水號碼
   ttAcal.aaa("curr_year", ooParm.isProcYymm);           	//-VARCHAR (6,0)  帳務年月
   ttAcal.aaa("card_no", ooParm.hhCardNo);             	//-VARCHAR (19,0)  卡號
   ttAcal.aaa("item_no", ooParm.hhItemNo);             	//-VARCHAR (2,0)  權益類別
   ttAcal.aaa("proj_code", ooParm.hhProjCode);           	//-VARCHAR (10,0)  適用專案
   ttAcal.aaa("free_type", ooParm.hhFreeType);           	//-VARCHAR (1,0)  免費次數類別
   ttAcal.aaa("free_cnt", ooParm.hhFreeCnt);            	//-INTEGER (4,0)  權益次數
   //007	use_cnt             	//-INTEGER (4,0)  已使用次數
   //008	free_per_amt        	//-DECIMAL (12,0)  每次折抵金額
   ttAcal.aaa("curr_proj_amt",ooParm.hhCurrProjAmt);       	//-DECIMAL (12,0)  當年專案金額
   ttAcal.aaa("last_proj_amt",ooParm.hhLastProjAmt);       	//-DECIMAL (12,0)  前年專案金額
   ttAcal.aaa("curr_year_consume",ooParm.hhCurrYearConsume);   	//-DECIMAL (12,0)  當年消費金額
   ttAcal.aaa("last_year_consume",ooParm.hhLastYearConsume);   	//-DECIMAL (12,0)  前年消費金額
   ttAcal.aaa("free_cal_date", sysDate);       	//-VARCHAR (8,0)  權益計算日期
   ttAcal.aaa("card_hldr_flag", "2");     	//-VARCHAR (1,0)  是否行員 1.行員,2.非行員
   ttAcal.aaa("debut_type",ooParm.hhDebutType);          	//-VARCHAR (1,0)  卡友身分 1.首發卡,2.新辦卡,3.舊卡友
   ttAcal.aaaYmd("crt_date");
   ttAcal.aaa("crt_user",hModUser);
   ttAcal.aaaFunc("cal_seqno", "ecs_modseq.nextval","");
//023	id_no               	//-VARCHAR (10,0)  身分證號碼
//024	major_card_no       	//-VARCHAR (19,0)  正卡卡號
//025	major_id_p_seqno    	//-VARCHAR (10,0)  正卡身分證流水號
//026	major_id            	//-VARCHAR (20,0)  正卡持卡人ID
   ttAcal.aaa("mod_type","A");            	//-VARCHAR (1,0)  異動類別
   ttAcal.aaa("acct_month", ooParm.isAcctYymm);          	//-VARCHAR (6,0)  帳務年月
   ttAcal.aaa("use_month", ooParm.isProcYymm);           	//-VARCHAR (6,0)  可使用年月
//030	old_card_no         	"//-VARCHAR (19,0)  舊卡卡號   		 "
   ttAcal.aaa("current_code","0");        	//-VARCHAR (1,0)  狀態碼
   ttAcal.aaa("purchase_date", ooParm.hhPurchDate);  //-VARCHAR (8,0)  消費日期
   ttAcal.aaa("data_from","2");           	//-VARCHAR (1,0)  資料來源
////034	chi_name            	"//-VARGRAPH(50,0)  中文姓名 	    	 "
   ttAcal.aaa("curr_max_amt", ooParm.hhCurrMaxAmt);  //-DECIMAL (9,0)  單筆最大金額 	   "
   ttAcal.aaa("curr_tot_cnt", ooParm.hhCurrTotCnt);  //-INTEGER (4,0)  累積消費筆數 		 "
   ttAcal.aaa("consume_type", ooParm.hhConsumeType);        	//-VARCHAR (1,0)  消費金額累積方式
//038	send_date           	//-VARCHAR (8,0)  名單寄送日期
   ttAcal.aaaModxxx(hModUser,hModPgm);

   if (ttAcal.ti <=0) {
      ttAcal.ti =ppStmtCrt("tt-cal",ttAcal.getSql());
   }

   sqlExec(ttAcal.ti,ttAcal.getParms());
   if(sqlNrow<=0){
      errmsg("insert cms_right_cal error ");
      errExit(rc);
   }

   return;
}

}

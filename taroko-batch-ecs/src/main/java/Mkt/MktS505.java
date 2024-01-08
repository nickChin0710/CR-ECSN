package Mkt;
/** DS動卡查核處理
 * 2019-0730   V1.00.00    JH          bugfix
 * 2018-1102:  V1.00.01    JH          modify
 * 2018-0925:  V1.00.02    Alex        initial
 * 109-12-11   V1.00.03    tanwei      updated for project coding standard
 */

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;

public class MktS505 extends BaseBatch {
private String progname = "DS動卡查核處理  109/12/11 V1.00.03";
//-----------------------------------------------------------------------------
private String hProcYm = "";
private int hAliveCondMon = 0;
private double hAliveCondPct = 0;
private double hAliveAmtPct = 0;
private String hDsCardNo = "";
private String hDsIssueDate = "";
private String hDsAliveFlag = "";
private String hDsAliveDate = "";
private String hDsSalesId = "";
private int hDsCardCnt = 0;
private int hDsAliveCnt = 0;
//-----------------------------------------------------------------------------
private int tiBill = -1;
private int tiDsCardU1 =-1;
private int tiDsCardU2 =-1;

//-----------------------------------------------------------------------------
public static void main(String[] args) {
   MktS505 proc = new MktS505();

//   proc.debug = true;
   //       proc.ddd_sql(true);

   proc.mainProcess(args);
   proc.systemExit(0);
}

//-----------------------------------------------------------------------------
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 2) {
      printf("Usage : MktS505 [yyyymm, batch_seq]");
      errExit(1);
   }

   dbConnect();

   if (liArg > 0) {
      hProcYm = args[0];
      callBatchSeqno(args[liArg - 1]);
   }
   callBatch(0, 0, 0);

   if (empty(hProcYm)) {
      hProcYm = commDate.dateAdd(hBusiDate, 0, -1, 0);
   }

   printf("-->處理月份=[%s]",hProcYm);

   selectMktDsParm1();
   selectMktDsCard();
   sqlCommit();
   summMktDsCard();

   sqlCommit();
   endProgram();
}

//-----------------------------------------------------------------------------
void selectMktDsParm1() throws Exception {
   sqlCmd = "select * "
         + " from mkt_ds_parm1 "
         + " where 1=1 "
         + " and param_type ='3' "
         + " and apr_flag ='Y' "
         + " and valid_date <= ? "
         + " order by valid_date desc "
         + commSqlStr.rownum(1)
   ;

   ppp(1, hBusiDate);

   sqlSelect();
   if (sqlNrow <= 0) {
      errmsg("select mkt_ds_parm1 error");
      errExit(1);
   }
   hAliveCondMon = colInt("alive_cond_mon");
   hAliveCondPct = colNum("alive_cond_pct");
   hAliveAmtPct = colNum("alive_amt_pct");
}

//-----------------------------------------------------------------------------
void selectMktDsCard() throws Exception {
   String lsBillDate = "";
   lsBillDate = commString.mid(commDate.dateAdd(hProcYm, 0, hAliveCondMon, 0), 0, 6) + "99";

   //       fetchExtend = "ds_card.";
   sqlCmd = " select A.card_no, "
         + " min(A.issue_date) as issue_date, "
         + " min(B.frst_consume_date) as frst_consume_date"
         + " from mkt_ds_card A join crd_card B on B.ori_card_no=A.card_no"
         + " where A.issue_date like ? "
         + " and A.alive_flag <> 'Y' "
         + " group by A.card_no "
   ;

   ppp(1, hProcYm + "%");

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      //            if(ti_bill<=0){
      //                sqlCmd = " select "
      //                         + " min(frst_consume_date) as frst_consume_date"
      //                         + " from crd_card "
      //                         + " where card_no in "
      //                         + " (select card_no from crd_card where ori_card_no = ?) "
      //                         + " and purchase_date <= ? "
      //                         + " and txn_code in ('05') "
      //                         ;
      //                ti_bill =ppStmt_crt("ti_bill","");
      //            }
      //            ppp(1,h_ds_card_no);
      //            ppp(ls_bill_date);
      //            sqlSelect(ti_bill);

      if (colEmpty("frst_consume_date"))
         continue;

      hDsCardNo = colSs("card_no");
      hDsIssueDate = colSs("issue_date");

      hDsAliveFlag = "Y";
      hDsAliveDate = colSs("frst_consume_date");
      updateMktDsCard(hDsCardNo);
   }
   closeCursor();
}

//-----------------------------------------------------------------------------
void updateMktDsCard(String aCardNo) throws Exception {
   if (tiDsCardU1 <=0) {
      sqlCmd = " update mkt_ds_card set "
            + " alive_flag = ? , "
            + " alive_date = ? , "
            + commSqlStr.modxxxSet(hModUser, hModPgm)
            + " where card_no = ? "
      ;
      tiDsCardU1 = ppStmtCrt("ti_ds_card_u_1", "");
   }

   ppp(1, hDsAliveFlag);
   ppp(hDsAliveDate);
   //--
   ppp(aCardNo);
//   ddd_sql(ti_ds_card_u_1);
   sqlExec(tiDsCardU1);
   if (sqlNrow <= 0) {
      sqlerr("update mkt_ds_card error");
      errExit(1);
   }

   return;
}

//=****************************************************************************
void summMktDsCard() throws Exception {

   double lmPct = 0, lmAmtPct = 0;

   fetchExtend = "ds_card2.";
   sqlCmd = " select sales_id , "
         + " count(*) as card_cnt , "
         + " sum(decode(alive_flag,'Y',1,0)) as alive_cnt "
         + " from mkt_ds_card "
         + " where issue_date like ? "
         +" and sales_id <>''"
         + " group by sales_id "
   ;

   ppp(1, hProcYm + "%");

   openCursor();

   while (fetchTable()) {
      hDsSalesId = colSs("ds_card2.sales_id");
      hDsCardCnt = colInt("ds_card2.card_cnt");
      hDsAliveCnt = colInt("ds_card2.alive_cnt");

      lmPct = (hDsAliveCnt / hDsCardCnt) * 100;
      lmAmtPct = 100;

      if (lmPct <= hAliveCondPct) lmAmtPct = hAliveAmtPct;

      if (tiDsCardU2 <=0) {
         sqlCmd = " update mkt_ds_card set "
               + " alive_card_pct = ? , "
               + " alive_amt_pct = ? "
               + " where issue_date like ? "
               + " and sales_id = ? "
         ;
         tiDsCardU2 = ppStmtCrt("tt_ds_card_u_2", "");
      }

      ppp(1, lmPct);
      ppp(lmAmtPct);
      ppp(hProcYm+"%");
      ppp(hDsSalesId);

//      ddd_sql(ti_ds_card_u_2);
      sqlExec(tiDsCardU2);
      if (sqlNrow <0) {
         sqlerr("update mkt_ds_card error");
         errExit(1);
      }
   }

   closeCursor();
}
}

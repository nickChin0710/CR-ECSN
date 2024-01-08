package Mkt;
/**
 * 2019-0730  V1.00.00    JH          IT-modify
 * 107/09/27  V1.00.01    Alex        initial                                 *
 * 109-12-11  V1.00.02    tanwei      updated for project coding standard     *
 */

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;

public class MktS504 extends BaseBatch {
   private String progname = "DS主管獎金統計處理 109/12/11 V1.00.02";
   //-----------------------------------------------------------------------------
   private String hProcYm = "";
   private String hValidDate = "";
   private int hSalesHireDay = 0;
   private int hSalesCnt = 0;
   private double hAmtPct = 0;
   private double hAmtMax = 0;
   private String hDataParmValidDate = "";
   private String hDsMangrId = "";
   private double hDsTotPoint = 0;
   private int hDsSalesCnt = 0;
   private int hDataTotSales = 0;
   private double hDataTotPoint = 0;
   private double hDataTotAmtPct = 0;
   private double hDataTotRealAmt = 0;
   private double hDataTotAmt = 0;
   private double hDataParmPoint = 0;
   private String hDataParmPointDesc = "";
   private String hDataMangrId = "";
   private double hDataParmAmt = 0;
   //-----------------------------------------------------------------------------
   com.DataSet dsParm = new com.DataSet();
   private SqlParm ttDsMangrI = null;

   //-----------------------------------------------------------------------------
   public static void main(String[] args) {
      MktS504 proc = new MktS504();

      //        proc.debug = true;
      //        proc.ddd_sql(true);

      proc.mainProcess(args);
      proc.systemExit(0);
   }

//-----------------------------------------------------------------------------

   @Override
   protected void dataProcess(String[] args) throws Exception {
      dspProgram(progname);

      int liArg = args.length;
      if (liArg > 2) {
         printf("Usage : MktS504 [yyyymm, batch_seq]");
         errExit(1);
      }

      dbConnect();

      //        comr = new CommRoutine(getDBconnect(), getDBalias());
      if (liArg > 0) {
         hProcYm = args[0];
         callBatchSeqno(args[liArg - 1]);
      }
      callBatch(0, 0, 0);

      if (empty(hProcYm)) {
         hProcYm = commDate.dateAdd(hBusiDate, 0, -1, 0);
      }

      printf("-->處理月份=[%s]", hProcYm);

      deleteMktDsMangrAmt();
      selectMktDsParm1();
      selectMktDsCard();
      sqlCommit();
      //       summ_mkt_ds_card();

      sqlCommit();
      endProgram();
   }

   //-----------------------------------------------------------------------------
   void deleteMktDsMangrAmt() throws Exception {
      sqlCmd = " delete mkt_ds_mangr_amt where stat_yymm = ? ";
      ppp(1, hProcYm);
      sqlExec(sqlCmd);
   }

   //-----------------------------------------------------------------------------
   void selectMktDsParm1() throws Exception {
      sqlCmd = " select * "
              + " from mkt_ds_parm1 "
              + " where param_type ='2' "
              + " and apr_flag='Y' "
              + " and valid_date <= ? "
              //         + " order by valid_date desc "
              + commSqlStr.rownum(1)
      ;

      ppp(1, hBusiDate);

      sqlSelect();

      if (sqlNrow <= 0) {
         sqlerr("select mkt_ds_parm1 N-find");
         errExit(1);
      }

      hValidDate = colSs("valid_date");
      hSalesHireDay = colInt("sales_hire_day");
      hSalesCnt = colInt("sales_cnt");
      hAmtPct = colNum("amt_pct");
      hAmtMax = colNum("amt_max");

      sqlCmd = " select * "
              + " from mkt_ds_parm1_detl "
              + " where param_type ='2' "
              + " and apr_flag='Y' "
              + " and valid_date = ? "
              + " order by add_point1 "
      ;

		this.sqlQuery(dsParm, sqlCmd, new Object[] { hValidDate });
   }

   //-----------------------------------------------------------------------------
   void selectMktDsCard() throws Exception {

      sqlCmd = " select "
              + " mangr_id , "
              + " sum(card_point) as tot_point , "
              + " count(distinct sales_id) as sales_cnt "
              + " from mkt_ds_card "
              + " where issue_date like ? "
              + " and sales_id in "
              + " (select sales_id from mkt_ds_sales where hire_date < ?) "
              + " group by mangr_id "
      ;

      ppp(1, hProcYm + "%");
      ppp(2, hProcYm + hSalesHireDay);

      fetchExtend = "ds_card.";
      openCursor();
      while (fetchTable()) {
         totalCnt++;

         hDsMangrId = colSs("ds_card.mangr_id");
         hDsTotPoint = colNum("ds_card.tot_point");
         hDsSalesCnt = colInt("ds_card.sales_cnt");

         hDataMangrId = hDsMangrId;
         hDataParmValidDate = hValidDate;
         hDataTotSales = hDsSalesCnt;
         hDataTotPoint = hDsTotPoint;
         setMsngrAmt(hDsTotPoint);
         hDataTotAmtPct = 100;

         if (hDataTotSales < hSalesCnt) hDataTotAmtPct = hAmtPct;

         hDataTotRealAmt = hDataTotAmtPct * hDataTotAmt;

         if (hDataTotRealAmt > hAmtMax) hDataTotRealAmt = hAmtMax;
         insertMktDsMangrAmt();
      }
      closeCursor();
   }

   //-----------------------------------------------------------------------------
   void setMsngrAmt(double amPoint) {
      for (int ii = 0; ii < dsParm.rowCount(); ii++) {
         if (amPoint < dsParm.colnum(ii, "add_point1")) break;
         hDataParmPoint = dsParm.colnum(ii, "add_point1");
         hDataParmPointDesc = dsParm.colss(ii, "add_point1") + " -- " + dsParm.colss(ii, "add_point2");
         hDataParmAmt = dsParm.colnum(ii, "point_amt");
         hDataTotAmt = amPoint * hDataParmAmt;
      }
   }

   //-----------------------------------------------------------------------------
   void insertMktDsMangrAmt() throws Exception {
      if (ttDsMangrI == null) {
         ttDsMangrI = new com.SqlParm();
         ttDsMangrI.sqlFrom = "insert into mkt_ds_mangr_amt ( "
                 + " stat_yymm ,"
                 + " mangr_id ,"
                 + " parm_valid_date ,"
                 + " parm_point ,"
                 + " parm_amt ,"
                 + " parm_point_desc ,"
                 + " tot_sale ,"
                 + " tot_point ,"
                 + " tot_amt ,"
                 + " tot_amt_pct ,"
                 + " tot_real_amt ,"
                 + " mod_time ,"
                 + " mod_pgm "
                 + " ) values ( "
                 + ttDsMangrI.pmkk(":stat_yymm ,")
                 + ttDsMangrI.pmkk(":mangr_id ,")
                 + ttDsMangrI.pmkk(":parm_valid_date ,")
                 + ttDsMangrI.pmkk(":parm_point ,")
                 + ttDsMangrI.pmkk(":parm_amt ,")
                 + ttDsMangrI.pmkk(":parm_point_desc ,")
                 + ttDsMangrI.pmkk(":tot_sale ,")
                 + ttDsMangrI.pmkk(":tot_point ,")
                 + ttDsMangrI.pmkk(":tot_amt ,")
                 + ttDsMangrI.pmkk(":tot_amt_pct ,")
                 + ttDsMangrI.pmkk(":tot_real_amt ,")
                 + " sysdate ,"
                 + ttDsMangrI.pmkk(":mod_pgm ")
                 + " ) "
         ;
         ttDsMangrI.pfidx = ppStmtCrt("tt_ds_mangr_i-A", ttDsMangrI.sqlFrom);
      }
      ttDsMangrI.ppp("stat_yymm", hProcYm);
      ttDsMangrI.ppp("mangr_id", hDataMangrId);
      ttDsMangrI.ppp("parm_valid_date", hDataParmValidDate);
      ttDsMangrI.ppp("parm_point", hDataParmPoint);
      ttDsMangrI.ppp("parm_amt", hDataParmAmt);
      ttDsMangrI.ppp("parm_point_desc", hDataParmPointDesc);
      ttDsMangrI.ppp("tot_sale", hDataTotSales);
      ttDsMangrI.ppp("tot_point", hDataTotPoint);
      ttDsMangrI.ppp("tot_amt", hDataTotAmt);
      ttDsMangrI.ppp("tot_amt_pct", hDataTotAmtPct);
      ttDsMangrI.ppp("tot_real_amt", hDataTotRealAmt);
      ttDsMangrI.ppp("mod_pgm", hModPgm);

      sqlExec(ttDsMangrI.pfidx, ttDsMangrI.getConvParm());
      if (sqlNrow <= 0) {
         sqlerr("insert mkt_ds_sales_amt error");
         errExit(1);
      }
      return;
   }

//-----------------------------------------------------------------------------
}

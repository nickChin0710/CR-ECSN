package Mkt;
/**
 * 2019-0730  V1.00.00    JH          modify
 * 107/09/27  V1.00.01    lex         initial                                 *
 * 109-12-10  V1.00.02    tanwei      updated for project coding standard     *
 */

import com.CommFunction;
import com.CommRoutine;
import com.SqlParm;
import com.BaseBatch;

public class MktS503 extends BaseBatch {
private String progname = "DS業務季獎金統計處理  109/12/10 V1.00.02";
CommFunction comm = new CommFunction();
CommRoutine comr = null;
//-----------------------------------------------------------------------------
private String hParmYymm = "";
private String hProcYm1 = "";
private String hProcYm2 = "";
private String hValidDate = "";
private int hSeasonPoint = 0;
private double hSeasonAmt = 0;
private double hDsTotAmt = 0;
private int hDsTotPoint = 0;
private String hDsSalesId = "";
//-----------------------------------------------------------------------------
private SqlParm ttDsSalesI = null;

//-----------------------------------------------------------------------------
public static void main(String[] args) {
   MktS503 proc = new MktS503();

   //proc.debug = true;
   //		proc.ddd_sql(true);

   proc.mainProcess(args);
   proc.systemExit(0);
}

//-----------------------------------------------------------------------------	
@Override
protected void dataProcess(String[] args) throws Exception{
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 2) {
      printf("Usage : MktS503 yyyymm [batch_seq]");
      errExit(1);
   }

   dbConnect();

   if (liArg > 0) {
      hParmYymm = args[0];
      callBatchSeqno(args[liArg - 1]);
   }
   callBatch(0, 0, 0);

   if (empty(hParmYymm)) {
      hParmYymm = commDate.dateAdd(hBusiDate, 0, -1, 0);
   }
   printf("-->參數年月=[%s]",hParmYymm);

   String ls_mm =commString.mid(hParmYymm,4,2);
   if (commString.ssIn(ls_mm,",01,02,03")) {
      hProcYm1 = commString.mid(hParmYymm, 0, 4) + "01";
      hProcYm2 = commString.mid(hParmYymm, 0, 4) + "03";
   } else if (commString.ssIn(ls_mm,",04,05,06")) {
      hProcYm1 = commString.mid(hParmYymm, 0, 4) + "04";
      hProcYm2 = commString.mid(hParmYymm, 0, 4) + "06";
   } else if (commString.ssIn(ls_mm,",07,08,09")) {
      hProcYm1 = commString.mid(hParmYymm, 0, 4) + "07";
      hProcYm2 = commString.mid(hParmYymm, 0, 4) + "09";
   } else if (commString.ssIn(ls_mm,",10,11,12")) {
      hProcYm1 = commString.mid(hParmYymm, 0, 4) + "10";
      hProcYm2 = commString.mid(hParmYymm, 0, 4) + "12";
   }

   printf("-->核卡統計期間: [%s]--[%s]",hProcYm1,hProcYm2);
   deleteMktDsSalesAmt();
   selectMktDsParm1();
   selectMktDsCard();

   sqlCommit();
   endProgram();

}

//-----------------------------------------------------------------------------	
void deleteMktDsSalesAmt() throws Exception {

   sqlCmd = "delete mkt_ds_sales_amt where stat_yymm = ? ";
   ppp(1, hProcYm2);

   sqlExec(sqlCmd);

}

//-----------------------------------------------------------------------------
void selectMktDsParm1() throws Exception {

   sqlCmd = " select * "
         + " from mkt_ds_parm1 "
         + " where 1=1 "
         + " and param_type ='3' "
         + " and apr_flag ='Y' "
         + " and valid_date <= ? "
         + " order by valid_date Desc "
         + commSqlStr.rownum(1)
   ;

   ppp(1, hBusiDate);

   sqlSelect();

   if (sqlNrow <= 0) {
      errmsg("select mkt_ds_parm1 error");
      errExit(1);
   }

   hValidDate = colSs("valid_date");  //-only 1 row;無有效日期-
   hSeasonPoint = colInt("season_point"); //每季總點數:
   hSeasonAmt = colNum("season_amt");  //季獎金:
}

//-----------------------------------------------------------------------------
void selectMktDsCard() throws Exception {

   fetchExtend = "ds.";
   sqlCmd = " select "
         + " sales_id , "
         + " sum(card_point) as tot_point "
         + " from mkt_ds_card "
         + " where issue_date between ? and ? "
         + " group by sales_id "
         + " order by sales_id "
   ;

   ppp(1, hProcYm1 + "01");
   ppp(2, hProcYm2 + "99");

   openCursor();

   while (fetchTable()) {
      totalCnt++;
      hDsSalesId = colSs("ds.sales_id");
      hDsTotPoint = colInt("ds.tot_point");
      if (hDsTotPoint >= hSeasonPoint) hDsTotAmt = hSeasonAmt;
      insertMktDsSalesAmt();
   }
   closeCursor();
}

//-----------------------------------------------------------------------------
void insertMktDsSalesAmt() throws Exception {

   if (ttDsSalesI == null) {
      ttDsSalesI = new com.SqlParm();
      ttDsSalesI.sqlFrom = " insert into mkt_ds_sales_amt ( "
            + " stat_yymm ,"
            + " sales_id ,"
            + " parm_valid_date ,"
            + " parm_point ,"
            + " tot_point ,"
            + " tot_amt ,"
            + " mod_time ,"
            + " mod_pgm "
            + " ) values ( "
            + ttDsSalesI.pmkk(":stat_yymm ,")
            + ttDsSalesI.pmkk(":sales_id ,")
            + ttDsSalesI.pmkk(":parm_valid_date ,")
            + ttDsSalesI.pmkk(":parm_point ,")
            + ttDsSalesI.pmkk(":tot_point ,")
            + ttDsSalesI.pmkk(":tot_amt ,")
            + " sysdate ,"
            + ttDsSalesI.pmkk(":mod_pgm ")
            + " ) "
      ;
      ttDsSalesI.pfidx = ppStmtCrt("tt_ds_sales_i-A", ttDsSalesI.sqlFrom);
   }

   ttDsSalesI.ppp("stat_yymm", hProcYm2);
   ttDsSalesI.ppp("sales_id", hDsSalesId);
   ttDsSalesI.ppp("parm_valid_date", hValidDate);
   ttDsSalesI.ppp("parm_point", hSeasonPoint);
   ttDsSalesI.ppp("tot_point", hDsTotPoint);
   ttDsSalesI.ppp("tot_amt", hDsTotAmt);
   ttDsSalesI.ppp("mod_pgm", hModPgm);

   sqlExec(ttDsSalesI.pfidx, ttDsSalesI.getConvParm());
   if (sqlNrow <= 0) {
      sqlerr("insert mkt_ds_sales_amt error");
      errExit(1);
   }
   return;

}
}

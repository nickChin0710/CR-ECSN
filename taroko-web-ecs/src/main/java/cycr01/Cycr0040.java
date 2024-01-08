/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/04/15  V1.00.01  Allen Ho       Initial                              *
 * 111/10/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 * 111-11-03  V1.00.03  Simon          alternative solution for ineffective * 
 *                                     "&nbsp;" derived from java program   *
 *                                                                          *
 ***************************************************************************/
package cycr01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycr0040 extends BaseEdit
{
  private final  String PROGNAME = "累積消費次數及金額查詢表處理程式111/11/03 V1.00.03";
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  busi.ecs.CommRoutine comr = null;
  cycr01.Cycr0040Func func = null;
  String kk1,kk2;
  String orgTabName = "mkt_card_consume";
  String controlTabName = "";
  int qFrom=0;
  String tranSeqStr = "";
  String batchNo = "";
  int errorCnt =0, recCnt =0, notifyCnt =0,colNum=0;
  int[] datachkCnt = {0,0,0,0,0,0,0,0,0,0};
  String[] uploadFileCol= new String[350];
  String[] uploadFileDat= new String[350];
  String[] logMsg       = new String[20];
  String upGroupType = "0";

  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception
  {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X"))
    {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "Q"))
    {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    }
    else if (eqIgno(wp.buttonCode, "R"))
    {//-資料讀取-
      strAction = "R";
      dataRead();
    }
    else if (eqIgno(wp.buttonCode, "A"))
    {// 新增功能 -/
      strAction = "A";
      insertFunc();
    }
    else if (eqIgno(wp.buttonCode, "U"))
    {/*  更新功能 */
      strAction = "U";
      updateFunc();
    }
    else if (eqIgno(wp.buttonCode, "D"))
    {/* 刪除功能 */
      deleteFunc();
    }
    else if (eqIgno(wp.buttonCode, "M"))
    {/* 瀏覽功能 :skip-page*/
      queryRead();
    }
    else if (eqIgno(wp.buttonCode, "S"))
    {/* 動態查詢 */
      querySelect();
    }
    else if (eqIgno(wp.buttonCode, "T"))
    {/* 動態查詢 */
      querySelect1();
    }
    else if (eqIgno(wp.buttonCode, "L"))
    {/* 清畫面 */
      strAction = "";
      clearFunc();
    }
    else if (eqIgno(wp.buttonCode, "NILL"))
    {/* nothing to do */
      strAction = "";
      wp.listCount[0] = wp.itemBuff("ser_num").length;
    }

    dddwSelect();
    initButton();
  }
  // ************************************************************************
  @Override
  public void queryFunc() throws Exception
  {
    if (queryCheck()!=0) return;
    wp.whereStr = "WHERE 1=1 "
            + sqlChkEx(wp.itemStr("ex_id_no"), "1", "")
            + sqlCol(wp.itemStr("ex_card_no"), "a.card_no", "like%")
            + sqlStrend(wp.itemStr("ex_acct_month_s"), wp.itemStr("ex_acct_month_e"), "a.acct_month")
    ;

    //-page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }
  // ************************************************************************
  @Override
  public void queryRead() throws Exception
  {
    if (wp.colStr("org_tab_name").length()>0)
      controlTabName = wp.colStr("org_tab_name");
    else
      controlTabName = orgTabName;

    wp.pageControl();

    wp.selectSQL = " "
            + "a.card_no, "
            + "max(card_type) as card_type,"
            + "sum(CONSUME_BL_AMT) as consume_bl_amt,"
            + "sum(CONSUME_CA_AMT) as consume_ca_amt,"
            + "sum(CONSUME_IT_AMT) as consume_it_amt,"
            + "sum(CONSUME_AO_AMT) as consume_ao_amt,"
            + "sum(CONSUME_ID_AMT) as consume_id_amt,"
            + "sum(CONSUME_OT_AMT) as consume_ot_amt,"
            + "sum(CONSUME_BL_AMT+CONSUME_IT_AMT-SUB_BL_AMT-SUB_IT_AMT) as GOOD_amt,"
            + "sum(CONSUME_ID_AMT+CONSUME_OT_AMT+CONSUME_AO_AMT+CONSUME_CA_AMT+SUB_BL_AMT+SUB_IT_AMT) as none_amt,"
            + "sum(CONSUME_BL_AMT+CONSUME_CA_AMT+CONSUME_IT_AMT+CONSUME_AO_AMT+CONSUME_ID_AMT+CONSUME_OT_AMT) as sum_amt,"
            + "max(major_id_p_seqno) as major_id_p_seqno,"
            + "max(decode(card_no,major_card_no,'正卡','附卡')) as sub_flag,"
            + "max(group_code) as group_code,"
            + "sum(CONSUME_BL_CNT) as consume_bl_cnt,"
            + "sum(CONSUME_CA_CNT) as consume_ca_cnt,"
            + "sum(CONSUME_IT_CNT) as consume_it_cnt,"
            + "sum(CONSUME_AO_CNT) as consume_ao_cnt,"
            + "sum(CONSUME_ID_CNT) as consume_id_cnt,"
            + "sum(CONSUME_OT_CNT) as consume_ot_cnt,"
            + "sum(CONSUME_BL_CNT+CONSUME_CA_CNT+CONSUME_IT_CNT+CONSUME_AO_CNT+CONSUME_ID_CNT+CONSUME_OT_CNT) as sum_cnt,"
            + "sum(CONSUME_BL_CNT+CONSUME_IT_CNT-SUB_BL_CNT-SUB_IT_CNT) as good_cnt,"
            + "sum(CONSUME_ID_CNT+CONSUME_OT_CNT+CONSUME_AO_CNT+CONSUME_CA_CNT+SUB_BL_CNT+SUB_IT_CNT) as NONE_cnt";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereOrder = " group by card_no"
            + " order by card_no"
    ;

    wp.pageCountSql = "select count(*) from ( "
            + " select distinct card_no"
            + " from "+ wp.daoTable +" "+wp.queryWhere
            + " )";

    pageQuery();
    listWkdata();
    wp.setListCount(1);
    if (sqlNotFind())
    {
      alertErr(appMsg.errCondNodata);
      return;
    }

    commCardType("comm_card_type");
    commGroupCode("comm_group_code");


    //list_wkdata();
    wp.setPageValue();
  }
  // ************************************************************************
  void listWkdata()  throws Exception
  {
    int totalBlAmt=0,totalCaAmt=0,totalItAmt=0,totalAoAmt=0,totalIdAmt=0,totalOtAmt=0;
    int totalBlCnt=0,totalCaCnt=0,totalItCnt=0,totalAoCnt=0,totalIdCnt=0,totalOtCnt=0;
    int totalGoodAmt=0,totalNoneAmt=0,totalSumAmt=0;
    int totalGoodCnt=0,totalNoneCnt=0,totalSumCnt=0;
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      wp.colSet(ii , "transtr"
              , wp.itemStr("ex_query_table")+"-"
                      + wp.itemStr("ex_acct_month_s")+"-"
                      + wp.itemStr("ex_acct_month_e")+"-"
                      + wp.itemStr("ex_acct_type")+"-"
      );


      totalBlAmt = totalBlAmt + (int)wp.colNum(ii,"consume_bl_amt");
      totalCaAmt = totalCaAmt + (int)wp.colNum(ii,"consume_ca_amt");
      totalItAmt = totalItAmt + (int)wp.colNum(ii,"consume_it_amt");
      totalAoAmt = totalAoAmt + (int)wp.colNum(ii,"consume_ao_amt");
      totalIdAmt = totalIdAmt + (int)wp.colNum(ii,"consume_id_amt");
      totalOtAmt = totalOtAmt + (int)wp.colNum(ii,"consume_ot_amt");
      totalSumAmt = totalSumAmt
              + (int)wp.colNum(ii,"consume_bl_amt")
              + (int)wp.colNum(ii,"consume_ca_amt")
              + (int)wp.colNum(ii,"consume_it_amt")
              + (int)wp.colNum(ii,"consume_ao_amt")
              +(int)wp.colNum(ii,"consume_id_amt")
              + (int)wp.colNum(ii,"consume_ot_amt");
      totalGoodAmt = totalGoodAmt + (int)wp.colNum(ii,"good_amt");
      totalNoneAmt = totalNoneAmt + (int)wp.colNum(ii,"none_amt");

      totalBlCnt = totalBlCnt + (int)wp.colNum(ii,"consume_bl_cnt");
      totalCaCnt = totalCaCnt + (int)wp.colNum(ii,"consume_ca_cnt");
      totalItCnt = totalItCnt + (int)wp.colNum(ii,"consume_it_cnt");
      totalAoCnt = totalAoCnt + (int)wp.colNum(ii,"consume_ao_cnt");
      totalIdCnt = totalIdCnt + (int)wp.colNum(ii,"consume_id_cnt");
      totalOtCnt = totalOtCnt + (int)wp.colNum(ii,"consume_ot_cnt");
      totalSumCnt = totalSumCnt
              + (int)wp.colNum(ii,"consume_bl_cnt")
              + (int)wp.colNum(ii,"consume_ca_cnt")
              + (int)wp.colNum(ii,"consume_it_cnt")
              + (int)wp.colNum(ii,"consume_ao_cnt")
              + (int)wp.colNum(ii,"consume_id_cnt")
              + (int)wp.colNum(ii,"consume_ot_cnt");
      totalGoodCnt = totalGoodCnt + (int)wp.colNum(ii,"good_cnt");
      totalNoneCnt = totalNoneCnt + (int)wp.colNum(ii,"none_cnt");

    }
/*
   wp.colSet("ex_total_msg1", "累計金額 "
                             +  "BL : " + toNosb(String.format("%,d", total_bl_amt),11) + "._"
                             +  "CA : " + toNosb(String.format("%,d", total_ca_amt),10) + "._"
                             +  "IT : " + toNosb(String.format("%,d", total_it_amt),10) + "._"
                             +  "AO : " + toNosb(String.format("%,d", total_ao_amt),8) + "._"
			     +  "ID : " + toNosb(String.format("%,d", total_id_amt),8) + "._" 
                             +  "OT : " + toNosb(String.format("%,d", total_ot_amt),8) + "._"
                             +  "總計: " + toNosb(String.format("%,d", total_sum_amt),11) + "._"
                             +  "有收益: " + toNosb(String.format("%,d", total_good_amt),11) + "._"
                             +  "無收益: " + toNosb(String.format("%,d", total_none_amt),11));
   wp.colSet("ex_total_msg2", "累計筆數 "
                             +  "BL : " + toNosb(String.format("%,d", total_bl_cnt),11) + "._"
                             +  "CA : " + toNosb(String.format("%,d", total_ca_cnt),10) + "._"
                             +  "IT : " + toNosb(String.format("%,d", total_it_cnt),10) + "._"
                             +  "AO : " + toNosb(String.format("%,d", total_ao_cnt),8) + "._"
			     +  "ID : " + toNosb(String.format("%,d", total_id_cnt),8) + "._" 
                             +  "OT : " + toNosb(String.format("%,d", total_ot_cnt),8) + "._"
                             +  "總計: " + toNosb(String.format("%,d", total_sum_cnt),11) + "._"
                             +  "有收益: " + toNosb(String.format("%,d", total_good_cnt),11) + "._"
                             +  "無收益: " + toNosb(String.format("%,d", total_none_cnt),11));

*/
/*
    wp.colSet("ex_total_msg1", "　　"
            + toNosb(String.format("%,d", totalBlAmt),11)
            + toNosb(String.format("%,d", totalCaAmt),11)
            + toNosb(String.format("%,d", totalItAmt),12)
            + toNosb(String.format("%,d", totalAoAmt),11)
            + toNosb(String.format("%,d", totalIdAmt),11)
            + toNosb(String.format("%,d", totalOtAmt),11)
            + toNosb(String.format("%,d", totalGoodAmt),14)
            + toNosb(String.format("%,d", totalNoneAmt),15)
            + toNosb(String.format("%,d", totalSumAmt),14)
    );
    wp.colSet("ex_total_msg2", "　　"
            + toNosb(String.format("%,d", totalBlCnt),11)
            + toNosb(String.format("%,d", totalCaCnt),11)
            + toNosb(String.format("%,d", totalItCnt),12)
            + toNosb(String.format("%,d", totalAoCnt),11)
            + toNosb(String.format("%,d", totalIdCnt),11)
            + toNosb(String.format("%,d", totalOtCnt),11)
            + toNosb(String.format("%,d", totalGoodCnt),14)
            + toNosb(String.format("%,d", totalNoneCnt),15)
            + toNosb(String.format("%,d", totalSumCnt),14)
    );
*/

    wp.colSet("ex_total_bl_amt",String.format("%,d", totalBlAmt));
    wp.colSet("ex_total_ca_amt",String.format("%,d", totalCaAmt));
    wp.colSet("ex_total_it_amt",String.format("%,d", totalItAmt));
    wp.colSet("ex_total_ao_amt",String.format("%,d", totalAoAmt));
    wp.colSet("ex_total_id_amt",String.format("%,d", totalIdAmt));
    wp.colSet("ex_total_ot_amt",String.format("%,d", totalOtAmt));
    wp.colSet("ex_total_good_amt",String.format("%,d", totalGoodAmt));
    wp.colSet("ex_total_none_amt",String.format("%,d", totalNoneAmt));
    wp.colSet("ex_total_sum_amt",String.format("%,d", totalSumAmt));

    wp.colSet("ex_total_bl_cnt",String.format("%,d", totalBlCnt));
    wp.colSet("ex_total_ca_cnt",String.format("%,d", totalCaCnt));
    wp.colSet("ex_total_it_cnt",String.format("%,d", totalItCnt));
    wp.colSet("ex_total_ao_cnt",String.format("%,d", totalAoCnt));
    wp.colSet("ex_total_id_cnt",String.format("%,d", totalIdCnt));
    wp.colSet("ex_total_ot_cnt",String.format("%,d", totalOtCnt));
    wp.colSet("ex_total_good_cnt",String.format("%,d", totalGoodCnt));
    wp.colSet("ex_total_none_cnt",String.format("%,d", totalNoneCnt));
    wp.colSet("ex_total_sum_cnt",String.format("%,d", totalSumCnt));

    return;

  }
  // ************************************************************************
  @Override
  public void querySelect() throws Exception
  {

    wp.colSet("bb_card_no",itemKk("data_k2"));
    wp.colSet("bb_transtr",itemKk("data_k3"));
    qFrom=1;
    dataRead();
  }
  // ************************************************************************
  public void querySelect1() throws Exception
  {
    controlTabName = orgTabName;

    kk1 = itemKk("data_k1");
    qFrom=2;
    dataRead();
  }
  // ************************************************************************
  @Override
  public void dataRead() throws Exception
  {
    if (controlTabName.length()==0)
    {
      if (wp.colStr("control_tab_name").length()==0)
        controlTabName = orgTabName;
      else
        controlTabName =wp.colStr("control_tab_name");
    }
    else
    {
      if (wp.colStr("control_tab_name").length()!=0)
        controlTabName =wp.colStr("control_tab_name");
    }
    wp.selectSQL = "hex(a.rowid) as rowid,"
            + " ROW_NUMBER()OVER() as ser_num, "
            + "a.acct_month,"
            + "'' as id_no,"
            + "'' as chi_name,"
            + "'' as major_id_no,"
            + "'' as major_chi_name,"
            + "a.card_no,"
            + "a.major_card_no,"
            + "a.acct_type,"
            + "a.card_note,"
            + "a.card_type,"
            + "a.group_code,"
            + "a.source_code,"
            + "a.consume_bl_amt,"
            + "a.consume_bl_cnt,"
            + "a.consume_ca_amt,"
            + "a.consume_ca_cnt,"
            + "a.consume_it_amt,"
            + "a.consume_it_cnt,"
            + "a.consume_ao_amt,"
            + "a.consume_ao_cnt,"
            + "a.consume_id_amt,"
            + "a.consume_id_cnt,"
            + "a.consume_ot_amt,"
            + "a.consume_ot_cnt,"
            + "consume_bl_amt+consume_it_amt-sub_bl_amt-sub_it_amt as GOOD_amt,"
            + "consume_bl_cnt+consume_it_cnt-sub_bl_cnt-sub_it_cnt as GOOD_cnt,"
            + "consume_id_amt+consume_ot_amt+consume_ao_amt+consume_ca_amt+sub_bl_amt+sub_it_amt as NONE_amt,"
            + "consume_id_cnt+consume_ot_cnt+consume_ao_cnt+consume_ca_cnt+sub_bl_cnt+sub_it_cnt as NONE_cnt,"
            + "a.id_p_seqno,"
            + "a.major_id_p_seqno";

    wp.daoTable = controlTabName + " a "
    ;
    wp.whereStr = "where 1=1 ";
    if (qFrom==0)
    {
      wp.whereStr = wp.whereStr
              + sqlCol(itemKk("data_k1"), "a.card_no")
              + sqlCol(itemKk("data_k2"), "a.bb_transtr_msg")
      ;
    }
    else if (qFrom==1)
    {
      wp.whereStr = wp.whereStr
              +  sqlCol(wp.colStr("bb_card_no") , "a.card_no")
      ;    // needed line don't delete
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      long listCnt = wp.colStr("bb_transtr").chars().filter(ch -> ch =='-').count();
      String[]  stra = new String[20];
      for (int inti=0;inti<=listCnt;inti++)
        stra[inti] = comm.getStr( wp.colStr("bb_transtr"), inti+1 ,"-");

      String transtrMsg = "查詢方式：";
      if (stra[0].equals("2"))
      {
        transtrMsg = transtrMsg
                + "入帳月  ";
      }
      else
      {
        transtrMsg = transtrMsg
                + "關帳月  ";
        wp.colSet("conytol_tab_name" , "mkt_card_consume");
      }

      if (stra[2].length()==0) stra[2]=wp.sysDate.substring(0,6);
      if (stra[1].length()>0)
        transtrMsg = transtrMsg
                + "查詢年月："+toDateFormat(stra[1])+"~"+toDateFormat(stra[2])+"   ";

      wp.colSet("bb_transtr_msg" , transtrMsg);

      wp.whereStr = wp.whereStr
              + sqlStrend(stra[1],stra[2], "a.acct_month")
      ;

      wp.whereStr = wp.whereStr

              + " order by acct_month"
      ;
    }
    else
    {
      wp.whereStr = wp.whereStr
              +  sqlRowId(kk1, "a.rowid")
      ;
    }

    pageSelect();
    wp.setListCount(1);
    wp.colSet("",itemKk("data_kN"));

    if (qFrom!=0)
    {
      commAcctType("comm_acct_type");
      commCardNote("comm_card_note");
      commCardType("comm_card_type");
      commGroupCode("comm_group_code");
      commSourceCode("comm_source_code");
    }
    datareadWkdata();
  }
  // ************************************************************************
  void datareadWkdata() throws Exception
  {
    String sql1="";

    sql1 = "select "
            + " id_no as id_no, "
            + " chi_name as chi_name "
            + " from crd_idno "
            + " where id_p_seqno = '"+wp.colStr("id_p_seqno")+"'"
    ;
    sqlSelect(sql1);
    wp.colSet("id_no", sqlStr("id_no"));
    wp.colSet("chi_name", sqlStr("chi_name"));

    sql1="";

    sql1 = "select "
            + " id_no as id_no, "
            + " chi_name as chi_name "
            + " from crd_idno "
            + " where id_p_seqno = '"+wp.colStr("major_id_p_seqno")+"'"
    ;
    sqlSelect(sql1);
    wp.colSet("major_id_no", sqlStr("id_no"));
    wp.colSet("major_chi_name", sqlStr("chi_name"));
  }
  // ************************************************************************
  public void saveFunc() throws Exception
  {
    cycr01.Cycr0040Func func =new cycr01.Cycr0040Func(wp);

    rc = func.dbSave(strAction);
    if (rc!=1) alertErr(func.getMsg());
    this.sqlCommit(rc);
  }
  // ************************************************************************
  @Override
  public void initButton()
  {
    if (wp.respHtml.indexOf("_detl") > 0)
    {
      this.btnModeAud();
    }
  }
  // ************************************************************************
  @Override
  public void dddwSelect()
  {
  }
  // ************************************************************************
  public int queryCheck() throws Exception
  {
    if ((itemKk("ex_card_no").length()==0)&&
            (itemKk("ex_id_no").length()==0))
    {
      alertErr("身份證號與卡號二者不可同時空白");
      return(1);
    }

    if (itemKk("ex_id_no").length()!=0)
      if ((itemKk("ex_id_no").length()!=10)&&
              (itemKk("ex_id_no").length()!=11)&&
              (itemKk("ex_id_no").length()!=8))
      {
        alertErr("正卡身份證字號/統編 只可輸入 8碼統編,10碼身份證字,11碼帳戶流水號 ");
        return(1);
      }

    if (itemKk("ex_query_table").equals("2"))
      orgTabName = "mkt_post_consume";
    else
      orgTabName = "mkt_card_consume";

    wp.colSet("org_tab_name", orgTabName);
    wp.colSet("control_tab_name", orgTabName);
    String sql1 = "";

    if (wp.itemStr("ex_card_no").length()!=0)
    {
      sql1 = "select a.id_p_seqno, "
              + "       a.chi_name "
              + "from crd_idno a,crd_card b "
              + "where  card_no  =  '"+ wp.itemStr("ex_card_no") +"'"
              + "and    a.id_p_seqno = b.id_p_seqno "
      ;

      if (wp.itemStr("ex_acct_type").length()!=0)
        sql1 = sql1
                + "and   b.acct_type  =  '"+ wp.itemStr("ex_acct_type").toUpperCase() +"' ";

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
      {
        alertErr(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
        return(1);
      }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      return(0);
    }

    if (wp.itemStr("ex_id_no").length()==10)
    {
      sql1 = "select a.id_p_seqno, "
              + "       a.chi_name "
              + "from  crd_idno a,act_acno b "
              + "where  id_no  =  '"+ wp.itemStr("ex_id_no").toUpperCase() +"' "
              + "and    a.id_p_seqno =  b.id_p_seqno "
              + "order by id_no_code "
      ;

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
      {
        sql1 = "select id_p_seqno "
                + "from   crd_idno  "
                + "where  id_p_seqno  =  '"+ sqlStr("id_p_seqno") +"' "
        ;

        sqlSelect(sql1);
        if (sqlRowNum <= 0)
          alertErr(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
        else
          alertErr(" 此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 已被瘦身");
        return(1);
      }
      wp.colSet("ex_corp_p_seqno","");
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name",sqlStr("chi_name"));

      return(0);
    }
    if (wp.itemStr("ex_id_no").length()==11)
    {
      sql1 = "select id_p_seqno, "
              + "       corp_p_seqno "
              + "from act_acno "
              + "where acct_key  = '"+ wp.itemStr("ex_id_no").toUpperCase() +"' "
              + "and   p_seqno = acno_p_seqno "
      ;

      sqlSelect(sql1);

      if (sqlRowNum <= 0)
      {
        alertErr(" 查無此帳戶查詢碼[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
        return(1);
      }
      if (sqlStr("id_p_seqno").length()>0)
      {
        wp.colSet("ex_corp_p_seqno","");
        wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
        sql1 = "select chi_name "
                + "from   crd_idno "
                + "where  id_p_seqno = '"+ sqlStr("id_p_seqno") +"' "
        ;
      }
      else
      {
        wp.colSet("ex_id_p_seqno","");
        wp.colSet("ex_corp_p_seqno",sqlStr("corp_p_seqno"));
        sql1 = "select chi_name "
                + "from   crd_corp "
                + "where  corp_p_seqno = '"+ sqlStr("corp_p_seqno") +"' "
        ;
      }
      sqlSelect(sql1);

      return(0);
    }
    if (wp.itemStr("ex_id_no").length()==8)
    {
      sql1 = "select chi_name, "
              + "corp_p_seqno "
              + "from   crd_corp "
              + "where corp_no  = '"+ wp.itemStr("ex_id_no").toUpperCase() +"' "
      ;
      sqlSelect(sql1);
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      wp.colSet("ex_id_p_seqno","");
      wp.colSet("ex_corp_p_seqno",sqlStr("corp_p_seqno"));

      return(0);
    }


    if (wp.itemStr("ex_card_no").length()!=0)
    {
      sql1 = "select a.id_p_seqno, "
              + "       a.chi_name "
              + "from crd_idno a,crd_card b "
              + "where  card_no  =  '"+ wp.itemStr("ex_card_no") +"'"
              + "and    a.id_p_seqno = b.id_p_seqno "
      ;

      if (wp.itemStr("ex_acct_type").length()!=0)
        sql1 = sql1
                + "and   b.acct_type  =  '"+ wp.itemStr("ex_acct_type").toUpperCase() +"' ";

      sqlSelect(sql1);
      if (sqlRowNum <= 0)
      {
        alertErr(" 查無此身分證號[ "+wp.itemStr("ex_id_no").toUpperCase() +"] 資料");
        return(1);
      }
      wp.colSet("ex_id_p_seqno",sqlStr("id_p_seqno"));
      wp.colSet("ex_chi_name",sqlStr("chi_name"));
      return(0);
    }


    return(0);
  }
  // ************************************************************************
  public String sqlChkEx(String ex_col, String sq_cond, String file_ext) throws Exception
  {
    if (sq_cond.equals("1"))
    {
      if (empty(wp.itemStr("ex_id_no"))) return "";
      String sql1 = "";
      String andStr = "";
      if (wp.colStr("ex_id_p_seqno").length()!=0)
      {
        sql1 = "select p_seqno "
                + "from act_acno "
                + "where  id_p_seqno = '"+ wp.colStr("ex_id_p_seqno") +"' "
        ;

        sqlSelect(sql1);
        if (sqlRowNum <= 0) return "";

        andStr = " and '"+ wp.colStr("ex_id_p_seqno") +"' in (id_p_seqno,major_id_p_seqno) ";
        andStr = andStr +  " and (p_seqno in (";
        for (int inti=0;inti<sqlRowNum;inti++)
        {
          if (inti!= sqlRowNum-1)
            andStr = andStr + " '"+ sqlStr(inti,"p_seqno")+"', ";
          else
            andStr = andStr + " '"+ sqlStr(inti,"p_seqno")+"') ";
        }
      }
      else
      {
        sql1 = "select p_seqno "
                + "from act_acno "
                + "where  corp_p_seqno  = '"+ wp.colStr("ex_corp_p_seqno") +"' "
        ;

        sqlSelect(sql1);
        if (sqlRowNum <= 0) return "";

        andStr = " and (p_seqno in (";
        for (int inti=0;inti<sqlRowNum;inti++)
        {
          if (inti!= sqlRowNum-1)
            andStr = andStr + " '"+ sqlStr(inti,"p_seqno")+"', ";
          else
            andStr = andStr + " '"+ sqlStr(inti,"p_seqno")+"') ";
        }
      }

      andStr = andStr + " ) ";
      return andStr;

    }

    return "";
  }
  // ************************************************************************
  public void commAcctType(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " chin_name as column_chin_name "
              + " from ptr_acct_type "
              + " where 1 = 1 "
              + " and   acct_type = '"+wp.colStr(ii,"acct_type")+"'"
      ;
      if (wp.colStr(ii,"acct_type").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_chin_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commCardNote(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " wf_desc as column_wf_desc "
              + " from ptr_sys_idtab "
              + " where 1 = 1 "
              + " and   wf_id = '"+wp.colStr(ii,"card_note")+"'"
              + " and   wf_type = 'CARD_NOTE' "
      ;
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_wf_desc");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commCardType(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " name as column_name "
              + " from ptr_card_type "
              + " where 1 = 1 "
              + " and   card_type = '"+wp.colStr(ii,"card_type")+"'"
      ;
      if (wp.colStr(ii,"card_type").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commGroupCode(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " group_name as column_group_name "
              + " from ptr_group_code "
              + " where 1 = 1 "
              + " and   group_code = '"+wp.colStr(ii,"group_code")+"'"
      ;
      if (wp.colStr(ii,"group_code").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_group_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void commSourceCode(String s1) throws Exception
  {
    String columnData="";
    String sql1 = "";
    for (int ii = 0; ii < wp.selectCnt; ii++)
    {
      columnData="";
      sql1 = "select "
              + " source_name as column_source_name "
              + " from ptr_src_code "
              + " where 1 = 1 "
              + " and   source_code = '"+wp.colStr(ii,"source_code")+"'"
      ;
      if (wp.colStr(ii,"source_code").length()==0)
      {
        wp.colSet(ii, s1, columnData);
        continue;
      }
      sqlSelect(sql1);

      if (sqlRowNum>0)
        columnData = columnData + sqlStr("column_source_name");
      wp.colSet(ii, s1, columnData);
    }
    return;
  }
  // ************************************************************************
  public void checkButtonOff() throws Exception
  {
    return;
  }
  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************
  public String toDateFormat(String date)
  {
    if (date.length()==6)
      return date.substring(0,4) + "/"+date.substring(4,6);
    else if (date.length()==8)
      return date.substring(0,4) + "/"+date.substring(4,6)+"/"+date.substring(6,8);
    else return date;
  }
  // ************************************************************************
  public String toNosb(String stra,int stralen)
  {
    if (stra.length()>=stralen) return stra;
    String retStra="";
    for (int inti=0;inti<(stralen-stra.length());inti++)
      retStra = retStra + "&nbsp;";
    return  retStra + stra;
  }


// ************************************************************************

}  // End of class

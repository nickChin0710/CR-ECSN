/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/21  V1.00.02   Allen Ho      Initial                              *
 * 111/11/28  V1.00.03  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package mktp01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0280Func extends FuncEdit
{
  private final String PROGNAME = "紅利積點兌換電子禮券批號設定作業處理程式110/07/21 V1.00.01";
  String controlTabName = "mkt_gift_bpexchg";

  public Mktp0280Func(TarokoCommon wr)
  {
    wp = wr;
    this.conn = wp.getConn();
  }
  // ************************************************************************
  @Override
  public int querySelect()
  {
    // TODO Auto-generated method
    return 0;
  }
  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }
  // ************************************************************************
  @Override
  public void dataCheck() {
    if (!this.ibAdd)
    {
    }


    if (this.isAdd()) return;

    //-other modify-
    sqlWhere = "where rowid = x'" + wp.itemStr("rowid") +"'"
            + " and nvl(mod_seqno,0)=" + wp.modSeqno();

    if (this.isOtherModify(controlTabName, sqlWhere))
    {
      errmsg("請重新查詢 !");
      return;
    }
  }
  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1 ;
  }
  // ************************************************************************
  @Override
  public int dbUpdate() {
    return rc;
  }
  // ************************************************************************
  @Override
  public int dbDelete() {
    return 1;
  }
  // ************************************************************************
  public int dbinsertMktGiftBatchno(String maxDatebno) throws Exception
  {
    strSql= " insert into mkt_gift_batchno ("
            + " create_date, "
            + " create_time, "
            + " ecoupon_bno, "
            + " ecoupon_date_s, "
            + " gift_group, "
            + " vendor_no, "
            + " tran_date_s, "
            + " tran_date_e, "
            + " ecoupon_cnt, "
            + " total_cnt, "
            + " mod_time,mod_user,mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,"
            + "sysdate,?,?)";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    wp.sysDate.substring(0,8)+ maxDatebno,
                    wp.itemStr("ex_effect_date_s"),
                    "3",
                    wp.itemStr("ex_vendor_no"),
                    wp.itemStr("ex_tran_date_s"),
                    wp.itemStr("ex_tran_date_e"),
                    wp.itemNum("ex_ecoupon_cnt"),
                    wp.itemNum("ex_total_cnt"),
                    wp.loginUser,
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增 mkt_gift_batchno 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbupdateMktGiftBpexchg(String maxDatebno) throws Exception
  {
    strSql = "update mkt_gift_bpexchg "
            + "set    ecoupon_bno =  '" + wp.sysDate.substring(0,8)+maxDatebno +"', "
            + "       ecoupon_date_s ='" + wp.itemStr("ex_effect_date_s") +"' "
            + " where gift_type     = '3' "
            + " and   ecoupon_bno   = '' "
            + " and   cellar_phone != '' "
            + " and   return_date   = '' ";

    if (wp.itemStr("ex_tran_date_s").length()!=0)
      strSql = strSql
              + " and tran_date  between  '"+ wp.itemStr("ex_tran_date_s") +"' "
              + "                and      '"+ wp.itemStr("ex_tran_date_e") +"' "
              ;
    else
      strSql = strSql
              + " and  ((tran_date <= '" + wp.itemStr("ex_tran_date_s") + "' "
              + "   and ecoupon_bno = '' ) "
              + "  or   ( a.tran_date between '" + wp.itemStr("ex_tran_date_s") + "'  "
              + "                     and     '" + wp.itemStr("ex_tran_date_e") + "'))  ";
    ;


    if (wp.itemStr("ex_vendor_no").length()!=0)
      strSql = strSql
              + " and  gift_no in ( "
              + "      select gift_no "
              + "      from   mkt_gift "
              + "      where  vendor_no = '"+ wp.itemStr("ex_vendor_no") +"') "
              ;

    Object[] param =new Object[] {};

    rc = sqlExec(strSql, param);

    return rc;
  }
// ************************************************************************

}  // End of class

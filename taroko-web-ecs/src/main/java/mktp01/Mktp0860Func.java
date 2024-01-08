/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 110-08-31  V1.00.03  Wendy Lu    程式修改                                                                         *
* 110/11/22  V1.00.04  jiangyingdong       sql injection                   *
***************************************************************************/
package mktp01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp0860Func extends FuncEdit {
  private String PROGNAME = "行銷通路活動回饋分析檔處理程式110/08/31 V1.00.03";
  String controlTabName = "mkt_channel_anal";

  public Mktp0860Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
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

	  int checkInt = checkDecnum(wp.itemStr("purchase_amt_s1"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg("一. 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg("一. 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg("一. 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_e1"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_rate_1"),3,2);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_amt_1"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_value_1"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("rank_amt_1"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_s2"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg("二. 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg("二. 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg("二. 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_e2"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_rate_2"),3,2);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_amt_2"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_value_2"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("rank_amt_2"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_s3"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg("三. 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg("三. 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg("三. 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_e3"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_rate_3"),3,2);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_amt_3"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_value_3"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("rank_amt_3"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_s4"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg("四. 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg("四. 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg("四. 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_e4"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_rate_4"),3,2);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_amt_4"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_value_4"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("rank_amt_4"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_s5"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg("五. 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg("五. 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg("五. 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("purchase_amt_e5"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_rate_5"),3,2);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[3]位 小數[2]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_amt_5"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("feedback_value_5"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("rank_amt_5"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("fund_amt"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }

	  checkInt = checkDecnum(wp.itemStr("gift_amt"),11,3);
	  if (checkInt!=0) 
	     {
	      if (checkInt==1) 
	         errmsg(" 格式超出範圍 : 整數[11]位 小數[3]位");
	      if (checkInt==2) 
	         errmsg(" 格式超出範圍 : 不可有小數位");
	      if (checkInt==3) 
	         errmsg(" 非數值");
	      return;
	     }


    if (this.isAdd())
      return;

    // -other modify-
		setString(wp.itemStr("rowid"));
    sqlWhere =
//        "where rowid = x'" + wp.itemStr("rowid") + "'" + " and nvl(mod_seqno,0)=" + wp.modSeqno();
        "where rowid = x?" + " and nvl(mod_seqno,0)=" + wp.modSeqno();

    if (this.isOtherModify(controlTabName, sqlWhere)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    return 1;
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
  public int checkDecnum(String decStr, int colLength, int colScale) {
	  if (decStr.length()==0) return(0);
	  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
	  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
	  decStr = decStr.replace("-","");
	  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
	  String[]  parts = decStr.split("[.^]");
	  if ((parts.length==1&&parts[0].length()>colLength)||
	      (parts.length==2&&
	       (parts[0].length()>colLength||parts[1].length()>colScale)))
	      return(1);
	  return(0);
  }

  public int dbUpdateMktChannelParm(int dateFlag) {
    String dateData = "";
    String userData = "";
    if (dateFlag == 0) {
      dateData = wp.sysDate;
      userData = wp.loginUser;
    }

    strSql =
        "update mkt_channel_parm set " + "feedback_apr_date  = ?, " + "feedback_apr_user  = ?, "
            + "mod_user  = ?, " + "mod_time  = sysdate, " + "mod_pgm   = ?, "
            + "mod_seqno = nvl(mod_seqno,0)+1 " + "where 1     = 1 " + "and   active_code  = ? ";

    Object[] param =
        new Object[] {dateData, userData, wp.loginUser, wp.modPgm(), wp.itemStr("active_code"),};

    rc = sqlExec(strSql, param);

    return rc;
  }
  // ************************************************************************


  // ************************************************************************

} // End of class

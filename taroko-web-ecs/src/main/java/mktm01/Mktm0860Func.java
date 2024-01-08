/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/09/05  V1.00.01   Allen Ho      Initial                              *
* 109-04-27  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *
* 110-08-31  V1.00.04  Wendy Lu      程式修改                                                                   *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0860Func extends FuncEdit {
  private String PROGNAME = "行銷通路活動回饋分析檔處理程式110/08/31 V1.00.04";
  String kk1,kk2;
  String controlTabName = "mkt_channel_anal";

  public Mktm0860Func(TarokoCommon wr) {
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
    if (!this.ibAdd) {
      kk1 = wp.itemStr("active_code");
      kk2 = wp.itemStr("active_seq");
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
	 public int dbUpdateMktChannelAnal(int dateFlag) 
	 {
	  String dateData ="";
	  String userData ="";
	  String flagData ="N";
	  wp.colSet("apr_date","");
	  wp.colSet("apr_user","");
	  if (dateFlag==0) 
	     {
	      dateData=wp.sysDate;
	      userData=wp.loginUser;
	      wp.colSet("apr_date",wp.sysDate);
	      wp.colSet("apr_user",wp.loginUser);
	      flagData="Y";
	     }
	  
	  strSql= "update mkt_channel_anal set "
	         + "apr_date  = ?, "
	         + "apr_user  = ?, "
	         + "apr_flag  = ?, "
	         + "mod_user  = ?, "
	         + "mod_time  = sysdate, "
	         + "mod_pgm   = ?, "
	         + "mod_seqno = nvl(mod_seqno,0)+1 "
	         + "where 1     = 1 "
	         + "and   active_code  = ? "
	         + "and   active_seq  = ? "
	         ;

	  Object[] param =new Object[]
	    {
	     dateData,
	     userData,
	     flagData,
	     wp.loginUser,
	     wp.modPgm(),
	     wp.itemStr("active_code"),
	     wp.itemStr("active_seq")
	    };

	  rc = sqlExec(strSql, param);

	  return rc;
  }

  // ************************************************************************
  public int dbUpdateMktChannelParm(int dateFlag) {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);

    String dateData = "";
    if (dateFlag == 0)
      dateData = wp.sysDate;

    strSql = "update mkt_channel_parm set " + "feedback_conf_date  = ?, " + "mod_user  = ?, "
        + "mod_time  = sysdate, " + "mod_pgm   = ?, " + "mod_seqno = nvl(mod_seqno,0)+1 "
        + "where 1     = 1 " + "and   active_code  = ? " + "and   feedback_apr_date = '' ";

    Object[] param = new Object[] {dateData, wp.loginUser, wp.modPgm(), wp.itemStr("active_code")};

    rc = sqlExec(strSql, param);

    return rc;
  }
// ************************************************************************


// ************************************************************************

}  // End of class

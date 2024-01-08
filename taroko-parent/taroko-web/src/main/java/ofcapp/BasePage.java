/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-13  V1.00.01  Zuwei        updated for project coding standard      *
*  109-04-21  V1.00.01  Zuwei        code format                              *
*  109-06-24  V1.00.02  Sunny        vip errmsg                                                                          *  
*  109-07-27  V1.00.01  Zuwei        coding standard      *
*  109-09-03  V1.00.03  JustinWu  revise zzVipColor()
*  109-09-09  V1.00.04  JustinWu  change vip_color from #00d000 to #CC0000
*  110-01-05  V1.00.05  JustinWu  remove the color in the style
*  110-01-08  V1.00.06  tanwei        修改意義不明確變量                                                                          * 
*  111-10-20  V1.00.07  Zuwei         sync method from mega (setCanAppr,apprDisabled) *
*  111-10-20  V1.00.08  Zuwei         sync method from mega (printVersion,modVersion) *
*  111-11-25  V1.00.09  Zuwei        add function apprDisabled(String col, String user) *
******************************************************************************/
package ofcapp;
/** UI畫面底層公用程式
 * 2019-1211   JH    appr_bankUnit()
 * 2019-1128   JH    ++appr_bankUnit()
 * 2019-1122   JH    不同程式權限
 * 2019-1021   JH    EcsApprove()
 * 2019-1017   JH    zz_vip_color()
 * 2019-1014   JH    queryMode.info for clear
 * 2019-1009   JH    err_pdf()
 * 2019-0909   JH    ++get_acct_key()
 * 2019-0905   JH    vip_color
 */

import taroko.com.*;
import java.lang.reflect.Method;
import java.text.*;
import java.util.Arrays;
import java.util.Date;

@SuppressWarnings({"unchecked", "deprecation"})
public class BasePage extends PageDAO {

  public String strAction = "";
  public int rc = 1;
  public int connIndex = 0;
  public String errPage = "TarokoDisplay";
  public String errPagePDF = "TarokoErrorPDF";
  String dddwCol = "";
  String dddwPreOption = "";
  // -appr-bankUnit-
  private String[] bankUser = new String[] {"", ""};
  private boolean bankAppr = false;

  // -程式權限-
  public boolean autQuery = false, autUpdate = false, autApprove = false, autPrint;

  // -DB差異-
  protected String sqlRowid = " hex(rowid) as rowid "; // DB2
  protected String sqlModDate = " to_char(mod_time,'yyyymmdd') as mod_date ";
  protected String sqlSysYmd = " to_char(sysdate,'yyyymmdd') ";

  protected AppMsg appMsg = new AppMsg();

  public enum AlignEm {
    left, center, right
  }

	protected void printVersion() {
	   String s1= modVersion();
	   if (wp==null || empty(s1))
	      return;
	   wp.log("[[[vvv]]]"+this.getClass().getSimpleName()+": "+s1);
	}
	protected void printVersion(String s1) {
	   if (wp==null || empty(s1))
	      return;
	   wp.log("[[[vvv]]]"+this.getClass().getSimpleName()+": "+s1);
	}
	public String modVersion() {
	   return "";
	}

  protected void sql2wp(String col) {
    wp.colSet(col, sqlStr(col));
  }

  protected void sql2wp(int num, String col) {
    wp.colSet(num, col, sqlStr(col));
  }

  protected void colReadOnly(String col) {
    wp.colSet(col, "readonly onkeydown=\"return false;\"");
  }

  protected void setRowNum(int num, int num1) {
    if (num1 < 10) {
      wp.serNum = "0" + num1;
    } else {
      wp.serNum = ("" + num1);
    }
    wp.setValue("SER_NUM", wp.serNum, num);
  }

  protected void setSerNum(int num, int num1) {
    setSerNum(num, "ser_num", num1);
  }

  protected void setSerNum(int num) {
    setSerNum(num, "ser_num", 1 + num);
  }

  protected void setSerNum(int num, String col, int number) {
    String lscol = col;
    if (empty(lscol))
      lscol = "ser_num";
    if (number < 10) {
      wp.serNum = "0" + number;
    } else {
      wp.serNum = "" + number;
    }
    wp.setValue(col, wp.serNum, num);
  }

  /*--db Connect-*/
  public void setConn(String strName) {
    connIndex = -1;
    if (empty(strName)) {
      strName = "Taroko";
    }

    TarokoParm tarokoParm = TarokoParm.getInstance();
    for (int i = 0; i < tarokoParm.getConnCount(); i++) {
      if (tarokoParm.getConnName()[i].equals(strName)) {
        connIndex = i;
        return;
      }
    }
  }

  /*-check Approve-*/
  public boolean reportApproveZz() {
    // -報表列印覆核-
    ofcapp.EcsApprove func = new ofcapp.EcsApprove(wp);
    return func.reportApprove();

    // String ls_user =wp.sss("approval_user");
    // String ls_passwd =wp.sss("approval_passwd");
    // wp.col_set("approval_user","");
    // wp.col_set("approval_passwd","");
    //
    // if (empty(ls_user) || empty(ls_passwd)) {
    // err_alert("[覆核主管/密碼]  不可空白");
    // return false;
    // }
    //
    // //--
    // if (eq_any(ls_user,this.loginUser())) {
    // err_alert("[覆核主管/維護經辦] 不可同一人");
    // return false;
    // }
    //
    // //-??權限-
    // auth_User(ls_user);
    // if (aut_print==false) {
    // err_alert("[覆核主管]: 對此作業無覆核權限");
    // return false;
    // }
    //
    // //-??passwd-
    // wp.col_set("apr_user","");
    // wp.col_set("apr_passwd","");
    //
    // return true;
  }

  public boolean checkApproveZz() {
    ofcapp.EcsApprove func = new ofcapp.EcsApprove(wp);
    if (func.onlineApprove(wp.itemStr2("approval_user"), wp.itemStr2("approval_passwd")) == false) {
      alertErr2(func.getMesg());
      return false;
    }

    wp.colSet("approval_user", "");
    wp.colSet("approval_passwd", "");
    return true;

    // if (check_approve(wp.item_ss("approval_user"),wp.item_ss("approval_passwd"))) {
    // wp.col_set("approval_user","");
    // wp.col_set("approval_passwd","");
    // return true;
    // }
    //
    // return false;
  }

  /**
   * sync from mega
   * 
   * @param col
   * @throws Exception
   */
  public void setCanAppr(String col) throws Exception {
     for(int ll=0; ll<wp.listCount[0]; ll++) {
        if (apprBankUnit(wp.colStr(ll,col),wp.loginUser)) {
           wp.colSet(ll,"can_appr","1");
        }
        else wp.colSet(ll,"can_appr","0");
     }
  }
  
  /**
   * 比對col值與user設置opt_disabled值，如果相同則設置opt_disabled為disabled
   * @param col
   * @param user
   */
  public void apprDisabled(String col, String user) {
        for (int ll = 0; ll < wp.listCount[0]; ll++) {
            if (!wp.colStr(ll, col).equals(user)) {
                wp.colSet(ll, "opt_disabled", "");
            } else
                wp.colSet(ll, "opt_disabled", "disabled");
        }
    }
  
  /**
   * sync from mega
   * 
   * @param col
   * @throws Exception
   */
  public void apprDisabled(String col) throws Exception {
     for(int ll=0; ll<wp.listCount[0]; ll++) {
        if (apprBankUnit(wp.colStr(ll,col),wp.loginUser)) {
           wp.colSet(ll,"opt_disabled","");
        }
        else wp.colSet(ll,"opt_disabled","disabled");
     }
	  }

  public boolean apprBankUnit(String modUser, String aprUser) {
    String lsModUser = nvl(modUser);
    String lsAprUser = commString.nvl(aprUser, wp.loginUser);
    if (empty(lsModUser) || empty(lsAprUser))
      return false;
    if (eqIgno(lsModUser, lsAprUser))
      return false;

    if (eqIgno(lsModUser, bankUser[0]) && eqIgno(lsAprUser, bankUser[1])) {
      return bankAppr;
    }
    bankUser[0] = lsModUser;
    bankUser[1] = lsAprUser;
    /*
     * select count(*) from sec_user A join sec_user B on
     * decode(A.bank_unitno,'109','Z09',A.bank_unitno)=decode(B.bank_unitno,'109','Z09',B.bank_unitno)
     * where 1=1 and A.usr_id ='0000' and B.usr_id='0002'
     */
    String sql1 =
        "select count(*) as unit_cnt"
            + " from sec_user A ,sec_user B"
            + " where decode(A.bank_unitno,'109','Z09',A.bank_unitno)=decode(B.bank_unitno,'109','Z09',B.bank_unitno)"
            + " and A.usr_id =? and B.usr_id =?";
    sqlSelect(sql1, bankUser);
    if (sqlRowNum <= 0)
      return false;

    bankAppr = (sqlInt("unit_cnt") > 0);
    return bankAppr;
  }

  public boolean checkApprove(String id, String passwd) {
    ofcapp.EcsApprove func = new ofcapp.EcsApprove(wp);
    if (!func.onlineApprove(id, passwd)) {
      alertErr2(func.getMesg());
      return false;
    }
    // -??passwd-
    wp.colSet("approval_user", "");
    wp.colSet("approval_passwd", "");
    return true;
  }

  public String userDeptNo() {
    if (empty(wp.loginUser))
      return "";

    String sql1 = "select usr_deptno" + " from sec_user " + " where usr_id =?";
    sqlSelect(sql1, new String[] {wp.loginUser});
    if (sqlRowNum > 0) {
      return sqlStr("usr_deptno");
    }

    return "";
  }

  public String getAcctKey(String idno, String type) {
    String lsAcctKey = "";
    if (idno.length() == 11)
      return idno;
    else if (idno.length() == 10)
      lsAcctKey = idno + "%";
    else if (idno.length() == 8)
      return idno + "000";
    else {
      return idno;
    }

    String lsAcctType = commString.nvl(type, "%");

    // String sql1 ="select A.current_code, B.acct_key, B.acct_type" +
    // "from crd_card A join act_acno B on A.acno_p_seqno=B.acno_p_seqno" +
    // "where B.acct_key like ? and B.acct_type like ?" +
    // "order by decode(A.current_code,'0','0','1'), B.acct_type, B.acct_key" +
    // commSqlStr.rownum(1);
    String sql1 =
        "select acct_key, acct_type from act_acno" + " where acct_key like ? and acct_type like ?"
            + " order by acct_key, acct_type" + commSqlStr.rownum(1);
    sqlSelect(sql1, new Object[] {lsAcctKey, lsAcctType});
    if (sqlRowNum > 0) {
      return sqlStr("acct_key");
    }

    return idno;
  }

  protected void sqlSelect(String sql1, Object obj1) {
    sqlSelect(sql1, new Object[] {obj1});
  }

  public void zzVipColor(String idCardno) {
    wp.colSet("zz_vip_code", "");
    wp.colSet("zz_vip_color", "");
    if (empty(idCardno)) {
      return;
    }

    // -失效ID-
    if (checkIdInvalid(idCardno) == 1) {
      return;
    }
    // -vip-
    String sql1 = "";
    if (idCardno.length() == 10) {
      if (this.isNumber(idCardno)) {
    	  // 以下將max(decode(vip_code,'5S',3 " + ",'WW',2,'4S',1,'6S',4,0)) as db_vip_code 刪除
        sql1 =
            "select  "
                + " count(*) as db_cnt " 
            	+ " from act_acno" 
                + " where 1=1"
                + " and acno_p_seqno =? "
                + " and vip_code<>''";
      } else {
        sql1 =
            "select  "
                + " count(*) as db_cnt " 
            	+ " from act_acno A, crd_idno B"
                + " where A.id_p_seqno=B.id_p_seqno" 
            	+ " and B.id_no =? "
            	+ " and A.vip_code<>''";
      }
      setString2(1, idCardno);
    } else if (idCardno.length() >= 15) {
      sql1 =
          "select  "
              + " count(*) as db_cnt" 
        	  + " from act_acno"
              + " where acno_p_seqno in (select acno_p_seqno from crd_card" + " where 1=1"
              + " and card_no =?) "
              + " and vip_code<>''";
      setString2(1, idCardno);
    } else if (idCardno.length() == 13) {
      // -acct_type+acct_key-
      sql1 =
          "select  "
              + " count(*) as db_cnt " 
        	  + " from act_acno" 
              + " where 1=1"
              + " and acct_key =? "
              + " and acct_type =? "
              + " and vip_code<>''";
      setString2(1, commString.mid(idCardno, 2)); // -acct_key-
      setString(commString.mid(idCardno, 0, 2)); // -acct_type-
    } else if (idCardno.length() >= 8 && idCardno.length() <= 11) {
      // -acct_type+acct_key-
      sql1 =
          "select  "
              + " count(*) as db_cnt " 
        	  + " from act_acno" 
              + " where 1=1"
              + " and acct_key =? "
              + " and vip_code<>''" 
              + commSqlStr.rownum(1);
      
//      setString2(1, commString.rpad(idCardno, 11, "0")); // -acct_key-
          setString2(1, idCardno); // -acct_key-
          
    } else {
      errmsg(String.format("無法判定 VIP, key[%s]", idCardno));
      return;
    }

    sqlSelect(sql1);
    if (sqlRowNum <= 0 || sqlInt("db_cnt") == 0)
      return;

//  // 2020-09-03 JustinWu
//    int liVip = sqlInt("db_vip_code");
//    if (liVip == 0) {
//      wp.colSet("zz_vip_code", "此卡友為4S_VIP客戶(V0-V8、WV)");
//      wp.colSet("zz_vip_color", "#00d000"); // RGB(0,208,0)
//    } else if (liVip == 1) {
//      wp.colSet("zz_vip_code", "此卡友為4S_VIP(4S)客戶");
//      wp.colSet("zz_vip_color", "#00d000"); // RGB(0,208,0)
//    } else if (liVip == 2) {
//      wp.colSet("zz_vip_code", "此卡友為5S_VIP(WW)客戶");
//      wp.colSet("zz_vip_color", "#b973ff"); // "RGB(185,115,255)");
//    } else if (liVip == 3) {
//      wp.colSet("zz_vip_code", "此卡友為5S_VIP(5S)客戶");
//      wp.colSet("zz_vip_color", "#b973ff"); // "RGB(185,115,255)");
//    } else if (liVip == 4) {
//      wp.colSet("zz_vip_code", "此卡友為6S_VIP(6S)客戶");
//      wp.colSet("zz_vip_color", "#ffff00"); // RGB(255,255,0)
//    }
    
    // 2020-09-03 JustinWu
    wp.colSet("zz_vip_code", "此卡友為VIP客戶");
    wp.colSet("zz_vip_color", "#CC0000"); // RGB(0,208,0)   舊顏色為#00d000

    if (!wp.colEmpty("zz_vip_code")) {
      wp.alertMesg(wp.colStr("zz_vip_code"));
      // wp.java_script("alert('"+wp.col_ss("zz_vip_code")+"');");
    }
  }

  private int checkIdInvalid(String idCardno) {
    if (empty(idCardno))
      return 0;

    String sql1 = "";
    if (idCardno.length() == 10) {
      if (this.isNumber(idCardno)) {
        // -acno_p_seqno-
        sql1 =
            "select  count(*) as db_cnt "
                + " from act_acno A join rsk_id_invalid B on B.id_p_seqno=A.id_p_seqno"
                + " where 1=1" + " and A.acno_p_seqno =?";
      } else {
        // -id_no-
        sql1 = "select count(*) as db_cnt " + " from rsk_id_invalid" + " where id_no =?";
      }
      setString2(1, idCardno);
    } else if (idCardno.length() >= 15) {
      sql1 =
          "select count(*) as db_cnt" + " from rsk_id_invalid"
              + " where id_p_seqno in (select id_p_seqno from crd_card" + " where card_no =?)";
      setString2(1, idCardno);
    } else if (idCardno.length() == 13 && !isNumber(idCardno)) {
      // -acct_type+acct_key-
      sql1 =
          "select count(*) as db_cnt "
              + " from act_acno A join rsk_id_invalid B on B.id_p_seqno=A.id_p_seqno"
              + " where 1=1" + " and A.acct_key =? and A.acct_type =?";
      setString2(1, commString.mid(idCardno, 2)); // -acct_key-
      setString(commString.mid(idCardno, 0, 2)); // -acct_type-
    } else {
      return 0;
    }
    sqlSelect(sql1);
    if (sqlRowNum <= 0 || sqlInt("db_cnt") <= 0)
      return 0;

    // -失效ID-
    wp.colSet("zz_vip_code", "此卡友為 [失效ID] 客戶");
    wp.colSet("zz_vip_color", "RGB(0,0,255)");
    if (!wp.colEmpty("zz_vip_code")) {
      wp.alertMesg(wp.colStr("zz_vip_code"));
    }

    return 1;
  }

  /* dddw_list */
  public String ddlbOption(String[] val) {
    StringBuffer sbOpt = new StringBuffer("");
    boolean lbFind = false;
    String ss = wp.optionKey;

    for (int ii = 0; ii < val.length; ii++) {
      if (!empty(ss) && eqAny(ss, val[ii])) {
        sbOpt.append("<option value='" + val[ii] + "' selected >" + val[ii] + "</option>"
            + wp.newLine);
        lbFind = true;
      } else {
        sbOpt.append("<option value='" + val[ii] + "'>" + val[ii] + "</option>" + wp.newLine);
      }
    }
    if (ss.length() > 0 && lbFind == false) {
      sbOpt.append("<option value='" + ss + "' selected >" + ss + "</option>" + wp.newLine);
    }

    return sbOpt.toString();
  }

  public String ddlbOption(String[] optionName, String[] optionTxt) {
    StringBuffer sbOpt = new StringBuffer("");
    boolean lbFind = false;
    String colName = wp.optionKey;

    for (int ii = 0; ii < optionName.length; ii++) {
      String lsTxt = optionName[ii];
      if (ii < optionTxt.length)
        lsTxt = optionTxt[ii];

      if (!empty(colName) && eqAny(colName, optionName[ii])) {
        sbOpt.append("<option value='" + optionName[ii] + "' selected >" + optionName[ii] + "." + lsTxt
            + "</option>" + wp.newLine);
        lbFind = true;
      } else {
        sbOpt.append("<option value='" + optionName[ii] + "'>" + optionName[ii] + "." + lsTxt + "</option>"
            + wp.newLine);
      }
    }
    if (colName.length() > 0 && lbFind == false) {
      sbOpt.append("<option value='" + colName + "' selected >" + colName + "</option>" + wp.newLine);
    }

    return sbOpt.toString();
  }

  public void dddwAddOption(String value, String text) {
    // <option value="*">*.通用</option>
    if (eqAny(wp.optionKey, value)) {
      dddwPreOption =
          "<option value='" + value + "' selected >" + text + "</option>" + wp.newLine;
      wp.optionKey = "";
      return;
    }

    dddwPreOption = "<option value='" + value + "' >" + text + "</option>" + wp.newLine;
  }

  // -DDLB-
  public void ddlbList(String strName, String obj1) {
    ddlbList(strName, wp.optionKey, obj1);
  }

  public void ddlbList(String strName, String colName, String obj1) {
    if (empty(strName) || empty(obj1))
      return;

    String lsOption = "";
    String lsClass = "";
    String lsMethod = "";

    // --
    int pnt2 = obj1.lastIndexOf(".");
    lsMethod = obj1.substring(pnt2 + 1);
    lsClass = obj1.substring(0, pnt2);
    if (empty(lsClass) || empty(lsMethod))
      return;

    try {
      Class<?> boClass = Class.forName(lsClass);
      // Method BoMethod = BoClass.getMethod(ls_method, String.class);
      @SuppressWarnings("rawtypes")
      Class[] cArg = new Class[2];
      cArg[0] = String.class;
      cArg[1] = boolean.class;
      Method boMethod = boClass.getMethod(lsMethod, cArg);
      Object bo = boClass.newInstance();

      lsOption = (String) boMethod.invoke(bo, colName, true);
      lsOption = dddwPreOption + lsOption;
      wp.colSet(strName, lsOption);
      dddwPreOption = "";
    } catch (Exception ex) {
      this.log("ddlb_List error-2: " + ex.getMessage());
    }
  }

  // -多筆DDDW-
  public void dddwList(int num, String col, String sql1) {
    if (posAny(sql1, "db_code") <= 0 || posAny(sql1, "db_desc") <= 0)
      return;

    if (empty(dddwCol) || eqIgno(dddwCol, col) == false) {
      // wp.dddSql_log =false;
      sqlSelect(sql1);
      // dddw_option ="";
    }
    dddwCol = col;

    // if (empty(wp.optionKey) && dddw_option.length()>0) {
    // wp.setValue(col,dddw_option, rr);
    // return;
    // }
    String lsOption = dddwSetData("db_code", "db_desc");
    wp.setValue(col, lsOption, num);
    // if (empty(wp.optionKey) && empty(dddw_option)) {
    // dddw_option = ls_option;
    // }
  }

  public void dddwList(int num, String col, String table, String idCode, String idDesc,
      String sWhere) throws Exception {
    String lsSql = "";
    if (idDesc.length() == 0 || idCode.equals(idDesc)) {
      lsSql =
          "select " + idCode + " as db_code" + ", '' as db_desc " + " from " + table + " "
              + sWhere;
    } else {
      lsSql =
          "select " + idCode + " as db_code" + ", " + idCode + "||'_'||" + idDesc
              + " as db_desc " + " from " + table + " " + sWhere;
    }

    dddwList(num, col, lsSql);
  }

  public void dddwList(String col, String table, String idCode, String sWhere) throws Exception {
    String lsSql = "";
    lsSql =
        "select " + idCode + " as db_code" + ", '' as db_desc" + " from " + table + " "
            + sWhere;

    dddwList(0, col, lsSql);
  }

  // -單筆DDDW--
  public void dddwList(String col, String sql1) {
    if (posAny(sql1, "db_code") > 0 && posAny(sql1, "db_desc") > 0) {
      sqlSelect(sql1);

      String lsOption = dddwSetData("db_code", "db_desc");
      wp.setValue(col, lsOption, 0);
    }
  }

  public void dddwShare(String col) {
    wp.setValue(col, dddwSetData("db_code", "db_desc"), 0);
  }

  public boolean dddwList(String col, String table, String idCode, String idDesc, String sWhere)
      throws Exception {
    if (idDesc.length() == 0 || idCode.equals(idDesc) || idDesc.indexOf("||") > 0) {
      return dropdownList(col, table, idCode, idDesc, sWhere);
    }

    return dropdownList(col, table, idCode, idCode + "||'_'||" + idDesc, sWhere);
  }

  public boolean dropdownList(String col, String table, String idCode, String idDesc,
      String sWhere) throws Exception {
    wp.varRows = 1000;
    String sql1 = "";
    if (empty(idDesc)) {
      sql1 = "select " + idCode + " as db_code" + ", '' as db_desc" + " from " + table;
    } else {
      sql1 =
          "select " + idCode + " as db_code" + ", " + idDesc + " as db_desc" + " from " + table;
    }
    if (empty(sWhere)) {
      sql1 += " order by 1 fetch first 999 rows only";
    } else {
      String ss = " " + sWhere.toLowerCase();

      if (ss.indexOf(" fetch ") >= 0 && ss.indexOf(" first ") > 0) {
        sql1 += " " + sWhere;
      } else {
        if (ss.indexOf(" order ") >= 0 && ss.indexOf(" by ") > 0) {
          sql1 += " " + sWhere + " fetch first 999 rows only";
        } else {
          sql1 += " " + sWhere + " order by 1 fetch first 999 rows only";
        }
      }
    }

    // wp.dddSql_log =false;
    sqlSelect(sql1);
    wp.setValue(col, dddwSetData("db_code", "db_desc"), 0);
    return true;
  }

  // public void dddw_Option(String col, String s_table, String id_code, String id_desc, String
  // s_where) throws Exception {
  // wp.varRows = 1000;
  // String sql1="";
  // if (empty(id_desc)) {
  // sql1 ="select "+id_code+" as db_code"
  // +", '' as db_desc"
  // +" from "+s_table;
  // }
  // else {
  // sql1 ="select "+id_code+" as db_code"
  // + ", " + id_desc + " as db_desc"
  // +" from "+s_table;
  // }
  // if (empty(s_where)) {
  // sql1 +=" order by 1 fetch first 999 rows only";
  // }
  // else {
  // String ss=" "+s_where.toLowerCase();
  //
  // if (ss.indexOf(" fetch ")>=0 && ss.indexOf(" first ")>0) {
  // sql1 +=" "+s_where;
  // }
  // else {
  // if (ss.indexOf(" order ")>=0 && ss.indexOf(" by ")>0) {
  // sql1 +=" "+s_where+" fetch first 999 rows only";
  // }
  // else {
  // sql1 +=" "+s_where+" order by 1 fetch first 999 rows only";
  // }
  // }
  // }
  // sqlSelect(sql1);
  // wp.optionKey ="";
  // wp.setValue(col, dddw_setData(col, "db_code", "db_desc"), 0);
  // }

  // 多重 動態選單處理
  // public String dddw_setData(String dynamicName, String codeName, String descName) {
  public String dddwOption(String col, String desc) {
    return dddwSetData(col, desc);
  }

  String dddwSetData(String codeName, String descName) {
    boolean lbFind = false;
    StringBuilder sbOption = new StringBuilder();
    String lsDesc = "", lsCode = "";
    // codeName = commString.nvl(codeName,"db_code");
    // descName =commString.nvl(descName,"db_desc");

    try {
      if (wp.initOption.length() != 0) {
        sbOption.append("<option value=''>").append(wp.initOption).append("</option> "); // .append(wp.newLine);
      }

      for (int i = 0; i < sqlRowNum; i++) {
        // wp.ddd("-->"+i+": code="+sql_ss(i,codeName)+", desc="+sql_ss(i,descName));
        lsCode = sqlStr(i, codeName);
        lsDesc = empty2Str(sqlStr(i, descName), lsCode);
        if (wp.optionKey.length() > 0 && eqAny(wp.optionKey, sqlStr(i, codeName))) {
          sbOption.append("<option value='").append(lsCode).append("' selected>").append(lsDesc)
              .append("</option> "); // wp.newLine);
          lbFind = true;
        } else {
          sbOption.append("<option value='").append(lsCode).append("'>").append(lsDesc)
              .append("</option> "); // wp.newLine);
        }
      }
      if (wp.optionKey.length() > 0 && lbFind == false) {
        sbOption.append("<option value='").append(wp.optionKey).append("' selected>")
            .append(wp.optionKey).append("</option> "); // .append(wp.newLine);
      }
    } catch (Exception ex) {
      wp.expMethod = "dddw_setData";
      wp.expHandle(ex);
    }
    String ss = dddwPreOption + sbOption.toString();
    dddwPreOption = "";
    return ss;
  }

  // -Button: disabled-[auth=A,U,D,Q,C,P,X,]=========================
  private boolean userTypeTest(String user) {
    String lsUser = user;
    if (empty(lsUser))
      lsUser = wp.loginUser;

    String sql1 = "select count(*) as xx_cnt from sec_user" + " where usr_id =? and usr_type ='T'";

    int llCnt = getCount(sql1, lsUser);
    if (llCnt > 0)
      return true;
    return false;
  }

  public boolean userAuthRun(String pgm) {
    // -使用者+程式權限------------
    if (empty(pgm))
      return false;
    if (userTypeTest(""))
      return true;

    String sql1 =
        "select count(*) as xx_cnt" + " from sec_authority A, sec_user B"
            + " where A.user_level =B.usr_level"
            + " and LOCATE(ucase(A.group_id),ucase(B.usr_group)) >0"
            + " and A.wf_winid =? and aut_query='Y'" + " and B.usr_id =?";
    // ppp(1, a_pgm);
    // ppp(wp.loginUser);
    // sqlSelect(sql1);
    // if (sql_nrow <= 0)
    // return false;
    // if (sql_int("xx_cnt") <= 0)
    // return false;
    int llCnt = getCount(sql1, pgm, wp.loginUser);
    return (llCnt > 0);
  }

  public boolean userAuthPgm(String pgm, int aiUpdate, int aiAppr, int aiPrint) {
    // -使用者+程式權限------------
    if (empty(pgm))
      return false;
    if (userTypeTest(""))
      return true;

    String sql1 =
        "select count(*) as xx_cnt," + " sum(decode(A.aut_update,'Y',1,0)) as cnt_update,"
            + " sum(decode(A.aut_approve,'Y',1,0)) as cnt_appr,"
            + " sum(decode(A.aut_print,'Y',1,0)) as cnt_print "
            + " from sec_authority A, sec_user B" + " where A.user_level =B.usr_level"
            + " and A.wf_winid =?" + " and LOCATE(ucase(A.group_id),ucase(B.usr_group)) >0"
            + " and B.usr_id =?";
    setString2(1, pgm);
    setString(wp.loginUser);
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return false;
    if (sqlInt("xx_cnt") <= 0)
      return false;

    if (aiUpdate > 0 && sqlNum("cnt_update") <= 0)
      return false;
    if (aiAppr > 0 && sqlNum("cnt_appr") <= 0)
      return false;
    if (aiPrint > 0 && sqlNum("cnt_print") <= 0)
      return false;

    return true;
  }

  public void queryModeClear() {
    // ref. wp.setQueryMode();
    wp.firstBrowse = "Y";
    wp.svFlag = "Y";
    wp.totalPage = 0;
    wp.totalRows = 0;
    wp.currPage = 0;
    wp.currRows = 0;
    wp.firstRow = 0;
  }

  public void authUser(String user) {
    authUser(user, "");
  }

  public void authPgm(String pgm) {
    authUser("", pgm);
  }

  public void authUser(String user, String pgm) {
    autQuery = false;
    autUpdate = false;
    autApprove = false;
    autPrint = false;

    if (commString.strIn2(user, ",EDS,DXC")) {
      autQuery = true;
      autUpdate = true;
      autApprove = true;
      autPrint = true;
      return;
    }

    // if (empty(wp.menuSeq))
    // return;

    String lsUser = commString.nvl(user, wp.loginUser);
    String lsPgm = commString.nvl(pgm, wp.modPgm());
    if (empty(lsUser) || empty(lsPgm))
      return;

    // -TTT-
    boolean flag = userTypeTest(lsUser);
    // String sql1 ="select count(*) as xx_cnt from sec_user where usr_id=? and usr_type='T'";
    // sqlSelect(sql1,new Object[]{ls_user});
    // if (sql_num("xx_cnt")>0) {
    if (flag) {
      autQuery = true;
      autUpdate = true;
      autApprove = true;
      autPrint = true;
      return;
    }

    String sql1 =
        "select sum(decode(A.aut_query,'Y',1,0)) as db_query, "
            + " sum(decode(A.aut_update,'Y',1,0)) as db_update, "
            + " sum(decode(A.aut_approve,'Y',1,0)) as db_approve, "
            // + " sum(decode(A.aut_print,'Y',1,0)) as db_print, "
            + " count(A.wf_winid) as db_cnt " + ", sum(decode(C.wf_update,'Y',1,0)) as update_flag"
            + ", sum(decode(C.wf_approve,'Y',1,0)) as appr_flag"
            + " from sec_authority A, sec_user B"
            + " left join sec_window C on C.wf_winid=A.wf_winid"
            + " where A.user_level = B.usr_level "
            + " and A.wf_winid = ? " 
            + " and  LOCATE(A.group_id,ucase(B.usr_group)) >0 "
            + " and B.usr_id = ? " ;
    setString(lsPgm);
    setString(lsUser);
    this.sqlSelect(sql1);
    if (sqlRowNum > 0 && sqlNum("db_cnt") > 0) {
      autQuery = (sqlNum("db_query") > 0);
      if (sqlNum("update_flag") > 0) {
        autUpdate = (sqlNum("db_update") > 0);
      }
      if (sqlNum("appr_flag") > 0) {
        autApprove = (sqlNum("db_approve") > 0);
      }
      // aut_print = (sql_num("db_print") > 0);
      return;
    }

    return;
  }

  public void btnOnAud(boolean bAdd, boolean bUpd, boolean bDel) {
    // get_userAuth();
    btnAddOn(bAdd);
    btnUpdateOn(bUpd);
    btnDeleteOn(bDel);
  }

  public void btnModeAud(String skey) {
    getPgmAuth();
    if (notEmpty(skey)) {
      wp.disabledKey = "Y";
      btnAddOn(false);
      btnUpdateOn(autUpdate);
      btnDeleteOn(autUpdate);
    } else {
      wp.disabledKey = "N";
      btnAddOn(autUpdate);
      btnUpdateOn(false);
      btnDeleteOn(false);
    }
  }

  public void btnModeAud(String pgm, String key) {
    if (empty(pgm))
      getPgmAuth();
    else
      authPgm(pgm);

    if (notEmpty(key)) {
      wp.disabledKey = "Y";
      btnAddOn(false);
      btnUpdateOn(autUpdate);
      btnDeleteOn(autUpdate);
    } else {
      wp.disabledKey = "N";
      btnAddOn(autUpdate);
      btnUpdateOn(false);
      btnDeleteOn(false);
    }
  }

  public void btnModeAud() {
    btnModeAud("", wp.colStr("rowid"));
  }

  public void btnModeUd(String key) {
    btnModeUd("", key);
  }

  public void btnModeUd(String pgm, String key) {
    if (empty(pgm)) {
      getPgmAuth();
    } else {
      authPgm(pgm);
    }

    if (notEmpty(key)) {
      btnAddOn(false);
      btnUpdateOn(autUpdate);
      btnDeleteOn(autUpdate);
    } else {
      btnAddOn(false);
      btnUpdateOn(autUpdate);
      btnDeleteOn(false);
    }
  }

  public void btnModeUd() {
    btnModeUd("", wp.colStr("rowid"));
  }

  public void buttonOff(String name1) {
    if (empty(name1)) {
      name1 = "button_off";
    }
    wp.colSet(name1, "disabled");
  }

  void getPgmAuth() {
    String lsModPgm = wp.itemStr2("mod_pgm");
    if (empty(lsModPgm) || eqIgno(lsModPgm, wp.menuSeq)) {
      autUpdate = wp.autUpdate();
      autApprove = wp.autApprove();
      return;
    }
    authPgm(lsModPgm);
  }

  public void btnAddOn(boolean flag) {
    buttonOff("btnAdd_disable");
    if (flag == false) {
      return;
    }
    // if (aut_update) {
    wp.colSet("btnAdd_disable", "");
    // }
  }

  public void btnUpdateOn(boolean flag) {
    buttonOff("btnUpdate_disable");
    if (flag == false) {
      return;
    }
    // if (aut_update) {
    wp.colSet("btnUpdate_disable", "");
    // }
  }

  public void btnDeleteOn(boolean flag) {
    buttonOff("btnDelete_disable");
    if (flag == false) {
      return;
    }
    // auth_Update();
    // if (aut_update) {
    wp.colSet("btnDelete_disable", "");
    // }
  }

  public void btnApprOn(boolean flag) {
    buttonOff("btnAappr_disable");
    if (flag == false && wp.autApprove() == false) {
      return;
    }
    wp.colSet("btnAppr_disable", "");
  }

  public void btnQueryOn(boolean flag) {
    // wp.col_set("btnQuery_disable", "disabled");
    buttonOff("btnQuery_disable");
    if (flag == false || wp.autQuery() == false) {
      return;
    }
    // if (wp.authData.indexOf("Q") >= 0) {
    wp.colSet("btnQuery_disable", "");
    // }
    return;
  }

  public void btnReadOn(boolean flag) {
    // wp.col_set("btnRead_disable", "disabled");
    buttonOff("btnRead_disable");
    if (flag == false) {
      return;
    }
    // if (wp.authData.length("Q") >= 0) {
    // wp.col_set("btnRead_disable", "");
    // }
    return;
  }

  public void btnConfirmOn(boolean flag) {
    // wp.col_set("btnConfirm_disable", "disabled");
    buttonOff("btnConfirm_disable");
    if (flag == false || wp.autApprove() == false) {
      return;
    }

    wp.colSet("btnConfirm_disable", "");
    return;
  }

  public void btnPdfOn(boolean flag) {
    // wp.col_set("btnPdf_disable", "disabled");
    buttonOff("btnPdf_disable");
    if (flag == false || wp.autPrint() == false) {
      return;
    }

    wp.colSet("btnPdf_disable", "");
    return;
  }

  public void btnExcelOn(boolean flag) {
    // wp.col_set("btnExcel_disable", "disabled");
    buttonOff("btnExcel_disable");
    if (flag == false || wp.autPrint() == false) {
      return;
    }

    wp.colSet("btnExcel_disable", "");
    return;
  }

  public void btnClearOn(boolean flag) {
    if (flag) {
      wp.colSet("btnClear_disable", "");
    } else {
      // wp.col_set("btnClear_disable", "disabled");
      buttonOff("btnClear_disable");
    }
    return;
  }

  public void btnClear2On(boolean flag) {
    if (flag) {
      wp.colSet("btnClear2_disable", "");
    } else {
      // wp.col_set("btnClear2_disable", "disabled");
      buttonOff("btnClear2_disable");
    }
    return;
  }

  // -Method-==============================================
  public boolean chkStrend(String strName, String colName) {
    if (empty(strName) || empty(colName)) {
      return true;
    }
    return nvl(strName).compareTo(nvl(colName)) <= 0;
  }

  public boolean condStrend(String strName, String colName) {
    return chkStrend(strName, colName);
  }

  public boolean icondStrend(String col1, String col2) {
    return chkStrend(wp.itemStr2(col1), wp.itemStr2(col2));
  }

  // public void setError(String col, String s1) {
  // rc = -1;
  // wp.setError(col, s1);
  // }

  public void errmsg(String strName) {
    rc = -1;
    wp.errorInput = true;
    wp.errMesg = strName;
    // wp.setValue("LEVEL_CODE", wp.levelCode, 0);
  }

  public void errmsg(String s1Err, String s2LevelCode) {
    rc = -1;
    wp.errorInput = true;
    wp.errMesg = s1Err;
    // wp.setValue("LEVEL_CODE", s2_levelCode, 0);
  }

  public void errmsg(String s1Err, Boolean b1Page) {
    errmsg(s1Err);
    if (b1Page) {
      wp.respHtml = "TarokoDisplay";
    }
  }

  public void alertErr(String s1Col, String s2Msg) {
    rc = -1;
    wp.setAlert(s1Col, (empty(s2Msg) ? getMesg() : s2Msg));
  }

  public void alertErr(String s2Msg) {
    rc = -1;
    wp.setAlert("", (empty(s2Msg) ? getMesg() : s2Msg));
  }

  public void alertErr2(String s2Msg) {
    rc = -1;
    wp.setAlert("", (empty(s2Msg) ? getMesg() : s2Msg));
  }

  public void alertPdfErr(String strName) {
    wp.respHtml = "TarokoErrorPDF";
    strName = empty(strName) ? getMesg() : strName;
    wp.setAlert("", (empty(strName) ? "無資料可印列" : strName));
  }

  public String loginUser() {
    return wp.loginUser;
  }

  public void dbCommit() throws Exception {
    // wp.commitDataBase();
    wp.commitOnly();
  }

  public void dbRollback() throws Exception {
    // wp.rollbackDataBase();
    wp.rollbackOnly();
  }

  public void sqlCommit(int rc) {
    try {
      if (rc == 1) {
        dbCommit();
      } else {
        dbRollback();
      }
    } catch (Exception ex) {
    }
  }

  // ======================================================
  protected void optOkflag(int num, int rc) {
    if (rc > 0)
      wp.colSet(num, "ok_flag", "V");
    else if (rc < 0)
      wp.colSet(num, "ok_flag", "X");
    else
      wp.colSet(num, "ok_flag", "!");
  }

  protected int optToIndex(String strName) {
    if (this.isNumber(strName) == false)
      return -1;

    int llSernum = toInt(strName) - 1;
    if (wp.pageRows < 999) {
      return (llSernum % wp.pageRows);
    }
    return llSernum;
  }

  protected void optNumKeep(int num, String[] opt) {
    for (int ii = 0; ii < num; ii++) {
      if (!checkBoxOptOn(ii, opt)) {
        continue;
      }
      wp.colSet(ii, "opt_on", "checked");
    }
  }

  protected void optNumKeep(int listNum) {
    String[] opt = wp.itemBuff("opt");
    optNumKeep(listNum, opt);
  }

  protected boolean optOn(int idx, String[] aaOpt) {
    if (aaOpt == null || aaOpt.length == 0)
      return false;
    if (idx < 0)
      return false;

    int liOpt = idx + 1;
    if (wp.pageRows < 999 && wp.currPage > 1) {
      liOpt += (wp.currPage - 1) * wp.pageRows;
    }
    String colName = "";
    if ((liOpt) < 10) {
      colName = "0" + liOpt;
    } else {
      colName = "" + liOpt;
    }

    return Arrays.asList(aaOpt).indexOf(colName) >= 0;
  }

  protected boolean checkBoxOptOn(int idx, String[] aOpt) {
    if (aOpt == null || aOpt.length == 0)
      return false;
    if (idx < 0)
      return false;

    int liOpt = idx + 1;
    if (wp.pageRows < 999 && wp.currPage > 1) {
      liOpt += (wp.currPage - 1) * wp.pageRows;
    }
    String colName = "";
    if ((liOpt) < 10) {
      colName = "0" + liOpt;
    } else {
      colName = "" + liOpt;
    }

    return Arrays.asList(aOpt).indexOf(colName) >= 0;
  }

  protected void optNumKeep(int listNum, String col, String onCol) {
    String[] opt = wp.itemBuff(col);
    for (int ii = 0; ii < listNum; ii++) {
      if (checkBoxOptOn(ii, opt)) {
        wp.colSet(ii, onCol, "checked");
      } else
        wp.colSet(ii, onCol, "");
    }
  }
  
  public void setSqlParmNoClear(boolean isNoClear) {
	  sqlParm.setSqlParmNoClear(isNoClear);
  }

  public boolean colIsEmpty(String col) {
    return empty(wp.colStr(0, col));
  }

  public boolean colHas(String col) {
    return empty(wp.colStr(col)) == false;
  }

  public boolean colEq(int num, String col, String strName) {
    String colName = wp.colStr(num, col);
    if (colName == null || strName == null) {
      return false;
    }

    return colName.equals(strName.trim());
  }

  public int itemPos(String strName, String col) {
    return pos(strName, wp.itemStr(col));
  }

  public boolean itemIsempty(String col) {
    return empty(wp.itemStr(col));
  }

  public boolean itemHas(String col) {
    return empty(wp.itemStr(col)) == false;
  }

  public boolean itemEq(String col, String strName) {
    try {
      String colName = wp.itemStr(col);
      if (colName == null || strName == null) {
        return false;
      }

      if (colName.trim().equals(strName.trim())) {
        return true;
      }
    } catch (Exception ex) {
    }

    return false;
  }

  public String itemUpper(String col1) {
    try {
      return wp.itemStr(col1).toUpperCase();
    } catch (Exception ex) {
    }
    return "";
  }

  public byte[] itemRowId(String col) {
    return wp.itemRowId(col);
  }

  /*-item-mothed-
   public void setNum(String col, double num, int rr) {
   wp.col_set(col,""+num, rr);
   }
   public double item_num(String col1) {
   return wp.item_num(col1);
   }
   public double item_num(int rr, String col) {
   return wp.item_num(rr, col);
   }
   public String[] item_buff(String col1) {
   return wp.getInBuffer(col1.trim().toUpperCase());
   }

   public String item_ss(String col1) {
   return wp.item_ss(col1);
   }

   public String item_ss(int rr, String col) {
   return wp.item_ss(rr, col);
   }

   public void item_set(String col1, String s1) {
   wp.item_set(col1, s1,0);
   }
   --*/
  // ==SQL-syntax===================================================
  // public void sql_parm(String s1) {
  // try {
  // ii_sql_parm++;
  // setString(ii_sql_parm, s1);
  // } catch (Exception ex) {
  // }
  // }
  //
  // public void sql_parm(int ii, String s1) {
  // try {
  // ii_sql_parm = ii;
  // setString(ii, s1);
  // return;
  // } catch (Exception ex) {
  // }
  // }

  public void item2ParmNvl(String name, String col, String strName) {
    try {
      this.setString(name, wp.itemNvl(col, strName));
    } catch (Exception e) {
    }
  }

  public void item2ParmNvl(String col, String strName) {
    try {
      this.setString(col, wp.itemNvl(col, strName));
    } catch (Exception e) {
    }
  }

  public void item2ParmStr(String name, String col) {
    try {
      this.setString(name, wp.itemStr(col));
    } catch (Exception e) {
    }
  }

  public void item2ParmStr(String col) {
    try {
      this.setString(col, wp.itemStr(col));
    } catch (Exception e) {
    }
  }

  public void item2ParmNum(String name, String col) {
    try {
      this.setDouble(name, wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  public void item2ParmNum(String col) {
    try {
      this.setDouble(col, wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  public void item2ParmInt(String name, String col) {
    try {
      this.setInt(name, (int) wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  public void item2ParmInt(String col) {
    try {
      this.setInt(col, (int) wp.itemNum(col));
    } catch (Exception e) {
    }
  }

  // public void sql_parm(double num) {
  // try {
  // ii_sql_parm++;
  // this.setDouble(ii_sql_parm, num);
  // } catch (Exception ex) {
  // }
  // return;
  // }
  //
  // public void sql_parm(int ii, double num) {
  // try {
  // ii_sql_parm = ii;
  // this.setDouble(ii, num);
  // } catch (Exception ex) {
  // }
  // return;
  // }

  public boolean sqlNotFind() {
    // return (wp.notFound.equals("Y") || (sql_nrow == 0));
    return (wp.notFound.equals("Y"));
  }
  
	public String inIdnoCrd(String col, String aIdno, String cond) {
		if (isEmpty(aIdno)) {
			return "";
		}
		if (isEmpty(col))
			col = "id_p_seqno";

		return " and " + col + " in ( select id_p_seqno from crd_idno where 1=1" + sqlCol(aIdno, "id_no", cond) + " )";
	}
	
	public String inAcnoAct(String col, String aAcctKey, String cond) {
		if (isEmpty(aAcctKey)) {
			return "";
		}
		if (isEmpty(col))
			col = "p_seqno";
		return " and " + col + " in ( select p_seqno from act_acno where 1=1" + sqlCol(aAcctKey, "acct_key", cond) + " )";
	}
	
	public String inAcnoDba(String col, String aAcctKey, String cond) {
		if (isEmpty(aAcctKey)) {
			return "";
		}
		if (isEmpty(col))
			col = "p_seqno";
		return " and " + col + " in ( select p_seqno from dba_acno where 1=1" + sqlCol(aAcctKey, "acct_key", cond) + " )";
	}
  
	public String sqlColIn(String col, String[] arr) {
		if (arr == null || arr.length == 0)
			return "";
		
		String lsSql = " and " + col + " in (''";
		for (int ii = 0; ii < arr.length; ii++) {
			if (isEmpty(arr[ii]))
				continue;
			lsSql += ", ? ";
			setString(arr[ii]);
		}
		lsSql += ")";
		return lsSql;
	}
	
	public String colAll(String strName, String col, String allCond) {
	    if (isEmpty(strName) || isEmpty(col))
	      return "";
	    if (strName.equals(allCond))
	      return "";

	    return sqlCol(strName, col);
	  }

  public String sqlCol(String strName, String col) {
		return sqlCol(strName, col, "=");
    // if (empty(s1)) {
    // return "";
    // }
    // return " and " + dbcol + " = '" + nvl(s1) + "'";
  }

  @Deprecated
  protected String sqlBetween(String parm1, String parm2, String col) {
    return commSqlStr.strend(wp.itemStr2(parm1), wp.itemStr2(parm2), col);
  }

  public void logSql() {
    if (notEmpty(wp.sqlCmd)) {
      log("sql=" + wp.sqlCmd);
    } else {
      log("sql=select " + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " "
          + wp.whereOrder);
    }
  }

  public void logSql(String strName) {
    if (notEmpty(wp.sqlCmd)) {
      log(strName + "=" + wp.sqlCmd);
    } else {
      log(strName + "=select " + wp.selectSQL + " from " + wp.daoTable + " " + wp.whereStr + " "
          + wp.whereOrder);
    }
  }
  
	public String col(String strName, String col, boolean bRequ) {
		if (bRequ && isEmpty(strName))
			return " and 1=2";
		String sql = commSqlStr.col(strName, col, bRequ);
		setString(strName);
		return sql;
	}
  
	public String col(double num1, String col, boolean bRequ) {
		if (bRequ && num1 == 0) {
			return " and 1=2";
		}
		String sql = commSqlStr.col(num1, col, bRequ);
		setDouble(num1);
		return sql;
	}

  public String sqlCol(String strName, String col, String cond1) {
		if (empty(strName) || empty(col)) {
			return "";
		}
		String sql = commSqlStr.col(strName, col, cond1); 
		setString(commSqlStr.paramStr);
		return sql;
  }

  // public String sql_like(String s1, String col) {
  // if (empty(s1.trim()) || empty(col)) {
  // return "";
  // }
  // return " and " + col + " like '" + s1 + "%'";
  // }

  public String sqlStrend(String strName, String colName, String col) {
	    if ( empty(col)) {
			return ""; 
		}
	    String sql = commSqlStr.strend(strName, colName, col);
		if (! empty(strName)) {
			setString(strName);
		}
		if (! empty(colName)) {
			setString(colName);
		}
		return sql;
    // String ls_sql = "";
    //
    // if (empty(col)) {
    // return "";
    // }
    // if (empty(s1.trim()) && empty(s2.trim())) {
    // return "";
    // }
    // if (!empty(s1.trim())) {
    // ls_sql += " and " + col + " >='" + s1 + "'";
    // }
    // if (!empty(s2.trim())) {
    // ls_sql += " and " + col + " <='" + s2 + "'";
    // }
    //
    // return ls_sql;
  }


  public String sqlRowId(String strName) {
    return sqlRowId(strName, "");
  }

  public String sqlRowId(String strName, String dbcol) {
    if (empty(strName)) {
      return "";
    }
    setString(nvl(strName));
    if (empty(dbcol)) {
      return " and hex(rowid) = ?";
    }
    
    return " and hex(" + dbcol + ") = ?";
  }

  protected void selectOK() {
    rc = 1;
    wp.notFound = "";
  }

  // public String sql_ymd() {
  // return "to_char(sysdate,'yyyymmdd')";
  // }
  //
  // public String sql_time() {
  // return "to_char(sysdate,'hh24miss')";
  // }
  //
  // public String sql_mod_seqno() {
  // return "nvl(mod_seqno,0)+1";
  // }
  // public String sql_mod_date() {
  // return "to_char(mod_time,'yyyymmdd') ";
  // }

  public boolean isOtherModify(String s1Table, String s2Where) {
    return this.selectCount(s1Table, s2Where) <= 0;
  }

  public boolean isOtherModify(String s1Table, String s2Where, Object[] param) {
    return this.selectCount(s1Table, s2Where, param) <= 0;
  }
  
  public String inIdno(String col, String aIdno, String cond) {
	    if (isEmpty(aIdno)) {
	      return "";
	    }
	    if (isEmpty(col))
	      col = "id_p_seqno";

	    return " and " + col + " in ( select id_p_seqno from crd_idno where 1=1"
	        + sqlCol(aIdno, "id_no", cond) 
	        + " union select id_p_seqno from dbc_idno where 1=1"
	        + sqlCol(aIdno, "id_no", cond) + " )";
	  }

  // -common method-=============================================

  public String getSysDate() {
    String dateStr = "";
    Date currDate = new Date();
    SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    dateStr = form1.format(currDate);

    return dateStr.substring(0, 8);
  }

  public boolean notEmpty(String strName) {
    return (!empty(strName));
  }

  public void itemNullToVal(String col, String strName) {
    if (itemIsempty(col)) {
      wp.itemSet(col, strName);
    }
  }

  public boolean isEmpty(String strName) {
    return empty(strName);
  }

  public boolean empty(String strName) {
    if (strName == null) {
      return true;
    }
    if (strName.trim().length() == 0) {
      return true;
    }

    return false;
  }

  public String empty2Str(String strName, String colName) {
    if (strName == null) {
      return colName.trim();
    }
    if (strName.trim().length() == 0) {
      return colName.trim();
    }

    return strName.trim();
  }

  public boolean isNumber(String strName) {
    if (empty(strName)) {
      return false;
    }

    // double lm_val=0;
    try {
      double num = Double.parseDouble(strName);
      if (Double.isNaN(num)) {
        return false;
      }
    } catch (Exception ex) {
      return false;
    }

    return true;
  }

  public String nvl(String strName) {
    if (strName == null) {
      return "";
    }

    return strName.trim();
  }

  // -data-Conert-------------

  public String intToStr(int num1) {
    /*
     * int number = 12345; DecimalFormat decimalFormat = new DecimalFormat("#,##0"); String
     * numberAsString = decimalFormat.format(number);
     */
    DecimalFormat decFMT = new DecimalFormat("###0");
    return decFMT.format(num1);
  }

  public String strMid(String strName, int indx1, int len1) {
    if (strName.length() == 0) {
      return "";
    }
    if (indx1 > strName.length() || len1 > strName.length()) {
      return strName;
    }
    if ((indx1 + len1) >= strName.length()) {
      return strName.substring(indx1);
    }

    return strName.substring(indx1, (indx1 + len1));
  }

  public String numToStr(double num1, String fmt1) {
    DecimalFormat decFMT;
    if (isEmpty(fmt1)) {
      decFMT = new DecimalFormat("#,##0");
    } else {
      decFMT = new DecimalFormat(fmt1);
    }
    return decFMT.format(num1);
  }

  public double toNum(String strName) {
    return commString.strToNum(strName);
    //
    // try {
    // if (empty(s1) || !wp.isNumber(s1)) {
    // return 0;
    // }
    // } catch (Exception ex) {
    // return 0;
    // }
    // return Double.parseDouble(s1);
  }

  public int toInt(String strName) {
    return commString.strToInt(strName);
  }

  public boolean eqIgno(String strName, String colName) {
    String strNameVal = nvl(strName);
    String colNameVol = nvl(colName);
    return strNameVal.equalsIgnoreCase(colNameVol);
  }

  public boolean eqAny(String strName, String colName) {
    if (strName == null || colName == null) {
      return false;
    }
    return strName.equals(colName);
  }

  public boolean neIgno(String strName, String colName) {
    String strNameVal = nvl(strName);
    String colNameVal = nvl(colName);
    return !strNameVal.equalsIgnoreCase(colNameVal);
  }

  public boolean neAny(String strName, String colName) {
    if (strName == null || colName == null) {
      return true;
    }
    return !strName.equals(colName);
  }

  public int pos(String strName, String colName) {
    if (strName == null || colName == null) {
      return -1;
    }
    if (strName.trim().length() == 0) {
      return -1;
    }
    if (colName.trim().length() == 0) {
      return -1;
    }

    return strName.trim().indexOf(colName.trim());
  }

  public int posAny(String strName, String colName) {
    if (strName == null || colName == null) {
      return -1;
    }
    if (strName.trim().length() == 0) {
      return -1;
    }
    if (colName.trim().length() == 0) {
      return -1;
    }

    return strName.toLowerCase().trim().indexOf(colName.toLowerCase().trim());
  }

  public void setListCount(int idx, String colID, String bgCol) {
    if (idx == 0) {
      wp.listCount[idx] = wp.selectCnt + wp.sumLine;
    } else {
      wp.listCount[idx - 1] = wp.selectCnt + wp.sumLine;
    }
    int liSerNum = wp.firstRow;
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      liSerNum++;
      if (liSerNum < 10) {
        wp.setValue(colID + "ser_num", "0" + liSerNum, ii);
      } else
        wp.setValue(colID + "ser_num", "" + liSerNum, ii);
      // -tr-BGcolor-
      if ((ii % 2) == 0) {
        wp.colSet(ii, bgCol, "rgba(255,255,230,0.5)");
      } else {
        wp.colSet(ii, bgCol, "rgba(255,255,179,0.5)");
      }
    }
  }

  public void setListBgcolor(int idx, String bgCol) {
    int liCnt = 0;
    if (idx == 0) {
      liCnt = wp.listCount[0];
    } else {
      liCnt = wp.listCount[idx - 1];
    }

    for (int ii = 0; ii < liCnt; ii++) {
      // -tr-BGcolor-
      if ((ii % 2) == 0) {
        wp.colSet(ii, bgCol, "rgba(255,255,230,0.5)");
      } else {
        wp.colSet(ii, bgCol, "rgba(255,255,179,0.5)");
      }
    }
  }

  double getNumber(String sql1, Object... objs) {
    return getNumber(wp.getConn(), sql1, objs);
  }

  int getCount(String sql1, Object... objs) {
    return (int) getNumber(wp.getConn(), sql1, objs);
  }

  // ==Log=============================================
  // public void ddd(String s1, String s2) {
  // //wp.showLogMessage("D","showScreen","started");
  // //wp.showLogMessage("D", s1, s2);
  // }

  public void log(String strName) {
    wp.log("-DDD->" + strName);
  }

  public void logSql2() {
    wp.log("-DDD->" + this.getClass().getSimpleName() + ":" + wp.sqlCmd);
  }

  public void log2(String strName) {
    System.out.println("-JJJ->" + this.getClass().getSimpleName() + ":" + strName);
  }

}

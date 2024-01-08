/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei       coding standard      *
*  109/09/04  V1.00.06    Zuwei     code scan issue    
*  109-07-27  V1.00.07  Zuwei       coding standard      *
*  110-01-08  V1.00.08  tanwei        修改意義不明確變量                                                                          * 
******************************************************************************/
package taroko.base;
/** SQL條件元件
 * 2019-1212   JH    ++mod_xxx
 * 2019-0618:  JH    --ufunc()
   2019-0509:  JH    ++ col_all(s1,col,all)
   2019-0416:  JH    col_in(col,ss[])
   2019-0318:  JH    sequence.nextval
 * 2018-0926:	JH		++col(,,boolean)
 * 2018-0330:	JH		++in_pseqno
 * 2018-0327:	JH		++in_id/corp_pseqno
 * 2018-0314:	JH		++rsk_ctrlseqno
 * 2018-0314:	JH		++seq_ibm_outgoing
 * V00.00	2017-10xx	JH		initial
 * */

public class CommSqlStr {

  public final String sqlID = "";
  public final String sqlDual = " SYSIBM.SYSDUMMY1 ";
  public final String sysYYmd = " to_char(sysdate,'yyyymmdd') ";
  public final String sysTime = " to_char(sysdate,'hh24miss') ";
  public final String sysdate = " sysdate "; // current timestamp
  public final String sqlDateTime = " to_char(sysdate,'yyyymmddhh24miss') ";
  public final String sqlDebitFlag = " decode(debit_flag,'Y','Y','N') as debit_flag ";
  public final String dddwUserType = "where usr_type ='4'";
  public final String whereBinno =
      " and ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9') ";
  public final String whereRowid=" and rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)";
  // -自動編號sequence--
  public final String modSeqnoSet = " mod_seqno =nvl(mod_seqno,0)+1";
  public final String seqIbmOutgoing = " seq_ibm_outgoing.nextval ";
  public final String seqEcsStop =
      " to_char(sysdate,'yy')||substr(lpad(ecs_stop.nextval,10,'0'),3) ";
  public final String rskCtrlseqno = " to_char(sysdate,'yymm')||lpad(rsk_ctrlseqno.nextval,6,'0') "; // " uf_rsk_ctrlseqno() ";
  
  public String paramStr = "";

  public boolean isDebit(String strName) {
    return strName.equalsIgnoreCase("Y");
  }

//  public String whereBinno(String cardNo) {
//    return " and '" + cardNo
//        + "' between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')";
//  }

  public String acctKey(String strName) {
    switch (strName.trim().length()) {
      case 8:
        return strName.trim() + "000";
      case 10:
        return strName.trim() + "0";
        // default:
        // return s1;
    }
    return strName.trim();
  }
  
//
//  // -in-sql->>>----------------------------------------------------------------------
//  // public String in_pseqno(String col,String a_param,String cond) {
//  public String inAcno(String col, String aAcctKey, String cond) {
//    if (isEmpty(aAcctKey)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "p_seqno";
//    return " and " + col + " in ( select p_seqno from act_acno where 1=1"
//        + col(aAcctKey, "acct_key", cond) + " union select p_seqno from dba_acno where 1=1"
//        + col(aAcctKey, "acct_key", cond) + " )";
//  }

//  // public String in_pseqno_act(String col,String a_param,String cond) {
//  public String inAcnoAct(String col, String aAcctKey, String cond) {
//    if (isEmpty(aAcctKey)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "p_seqno";
//    return " and " + col + " in ( select p_seqno from act_acno where 1=1"
//        + col(aAcctKey, "acct_key", cond) + " )";
//  }

  // public String in_pseqno_dba(String col,String a_param,String cond) {
//  public String inAcnoDba(String col, String aAcctKey, String cond) {
//    if (isEmpty(aAcctKey)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "p_seqno";
//    return " and " + col + " in ( select p_seqno from dba_acno where 1=1"
//        + col(aAcctKey, "acct_key", cond) + " )";
//  }

  // public String in_idpseqno(String col,String param, String cond) {
//  @Deprecated
//  public String inIdno(String col, String aIdno, String cond) {
//    if (isEmpty(aIdno)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "id_p_seqno";
//
//    return " and " + col + " in ( select id_p_seqno from crd_idno where 1=1"
//        + col(aIdno, "id_no", cond) + " union select id_p_seqno from dbc_idno where 1=1"
//        + col(aIdno, "id_no", cond) + " )";
//  }

  // public String in_idpseqno_crd(String col,String param,String cond) {
//  public String inIdnoCrd(String col, String aIdno, String cond) {
//    if (isEmpty(aIdno)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "id_p_seqno";
//
//    return " and " + col + " in ( select id_p_seqno from crd_idno where 1=1"
//        + col(aIdno, "id_no", cond) + " )";
//  }

//  // public String in_idpseqno_dbc(String col,String param,String cond) {
//  public String inIdnoDbc(String col, String aIdno, String cond) {
//    if (isEmpty(aIdno)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "id_p_seqno";
//
//    return " and " + col + " in ( select id_p_seqno from dbc_idno where 1=1"
//        + col(aIdno, "id_no", cond) + " )";
//  }

//  // public String in_corp_pseqno(String col,String param,String cond) {
//  public String inCorp(String col, String aCorpNo, String cond) {
//    if (isEmpty(aCorpNo) || isEmpty(col)) {
//      return "";
//    }
//    if (isEmpty(col))
//      col = "corp_p_seqno";
//
//    return " and " + col + " in ( select corp_p_seqno from crd_corp where 1=1"
//        + col(aCorpNo, "corp_no", cond) + " )";
//  }

  // ->>>----------------------------------------------------------------------
  public String mchtName(String chiCol, String engCol) {
    String lsChi = chiCol;
    String lsEng = engCol;
    if (isEmpty(lsChi))
      lsChi = "mcht_chi_name";
    if (isEmpty(lsEng))
      lsEng = "mcht_eng_name";

    return " decode(replace(" + lsChi + ",'　',''),''," + lsEng + "," + lsChi + ") ";
  }

  // public String debit_flag(String tid) {
  // if (tid.length()>0) {
  // return " "+"decode(nvl("+tid+".debit_flag,'N'),'Y','Y','N') as debit_flag ";
  // }
  // return " "+"decode(nvl(debit_flag,'N'),'Y','Y','N') as debit_flag ";
  // }
  // public String ufunc(String s1) {
  // return " "+sqlID+ltrim(s1)+" ";
  // }
  public String nvl(String strName, String colName) {
    return " uf_nvl(" + strName + "," + colName + ") ";
    // return " decode("+s1+",'',"+s2+","+s1+") ";
  }

  public String ufunc(String strName) {
    return " " + sqlID + ltrim(strName) + " ";
  }

  private String likeReplace(String strName) {
    if (strName.indexOf("_") < 0)
      return strName;

    char[] arr = strName.toCharArray();
    StringBuilder ss = new StringBuilder(arr.length * 2);
    for (int ii = 0; ii < arr.length; ii++) {
      if (arr[ii] == '_')
        ss.append("\\_");
      else
    	  ss.append(String.valueOf(arr[ii]));
    }
    return ss.toString().trim();
  }

//  @Deprecated
//  public String colAll(String strName, String col, String allCond) {
//    if (isEmpty(strName) || isEmpty(col))
//      return "";
//    if (strName.equals(allCond))
//      return "";
//
//    return col(strName, col);
//  }

  public String col(String strName, String col, boolean bRequ) {
    if (bRequ && isEmpty(strName))
      return " and 1=2";
    return col(strName, col);
  }

  public String col(String val, String col, String cond) {
    StringBuffer sqlSb = new StringBuffer();

    if (isEmpty(val) || isEmpty(col))
      return "";
    String sqlCond = cond;
    if (isEmpty(cond))
      sqlCond = "=";

    if (sqlCond.equals("=")) {
    	paramStr = val;
      sqlSb.append(" and ").append(col).append(" ").append(sqlCond).append(" ? ");
      return sqlSb.toString();
    }
    String ss = "";
    if (sqlCond.equalsIgnoreCase("like%")) {
      if (val.indexOf("_") >= 0) {
        ss = likeReplace(val) + "%";
        sqlSb.append(" and ").append(col).append(" like ? escape '\\'");
      } else{
    	  ss = val+"%";
    	  sqlSb.append(" and ").append(col).append(" like ? ");
      }
    } else if (sqlCond.equalsIgnoreCase("%like")) {
      if (val.indexOf("_") >= 0) {
        ss = "%" + likeReplace(val);
        sqlSb.append(" and ").append(col).append(" like ? escape '\\'");
      } else{
    	ss = "%" + val;
    	sqlSb.append(" and ").append(col).append(" like ? ");
      }
    } else if (sqlCond.equalsIgnoreCase("%like%")) {
      if (val.indexOf("_") >= 0) {
        ss = "%" + likeReplace(val) + "%";
        sqlSb.append(" and ").append(col).append(" like ? escape '\\'");
      } else{
    	  ss = "%" + val + "%";
    	  sqlSb.append(" and ").append(col).append(" like ? ");
      }
    } else {
      ss = val;
      sqlSb.append(" and ").append(col).append(sqlCond).append(" ? ");
    }
    
    paramStr = ss;
    return sqlSb.toString();
  }

  public String rownum(int num1) {
    return " fetch first " + num1 + " rows only ";
  }


  /**
   * This method may cause SQL injection, if s1 comes from the input of a user.
   * To prevent SQL injection, please change to use the following code:  <br><code>sqlScript += " and hex(rowid) = ? "<br>setString(rowid)</code>
   * @param strName
   * @return
   */
  @Deprecated 
  public String whereRowid(String strName) {
    return " and rowid =x'" + strName + "'";
  }

  public byte[] strToRowid(String strName) {
    int len = strName.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(strName.charAt(i), 16) << 4) + Character.digit(strName.charAt(i + 1), 16));
    return data;
  }

  // static byte[] hex2Byte(String s)
  // {
  // int len = s.length();
  // byte[] data = new byte[len / 2];
  // for (int i = 0; i < len; i += 2)
  // data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
  // + Character.digit(s.charAt(i+1), 16));
  // return data;
  // }

  @Deprecated
  public String colIn(String col, String[] arr) {
    if (arr == null || arr.length == 0)
      return "";
    String lsSql = " and " + col + " in (''";
    for (int ii = 0; ii < arr.length; ii++) {
      if (isEmpty(arr[ii]))
        continue;
      lsSql += ",'" + arr[ii] + "'";
    }
    lsSql += ")";
    return lsSql;
  }

  public String col(String strName, String col) {
    return col(strName, col, "=");
  }

  public String col(double num1, String col, boolean bRequ) {
    if (bRequ && num1 == 0) {
      return " and 1=2";
    }
    return col(num1, col);
  }

  public String col(double num1, String col, String cond) {

    String lsCond = cond;
    if (isEmpty(col))
      return "";
    if (isEmpty(cond))
      lsCond = "=";

    return " and " + col + " " + lsCond + " ? ";
  }

  public String col(double num1, String col) {
    return col(num1, col, "=");
  }

  // -SQL=between-
  public String strend(String strName, String colName, String col) {
    return col(strName, col, ">=") + col(colName, col, "<=");
  }

  public String strend(double num1, double num2, String col) {
    return col(num1, col, ">=") + col(num2, col, "<=");
  }

  public String rtrim(String strName) {
    int i = strName.length() - 1;
    while (i >= 0 && Character.isWhitespace(strName.charAt(i))) {
      i--;
    }
    return strName.substring(0, i + 1);
  }

  public String ltrim(String strName) {
    int i = 0;
    while (i < strName.length() && Character.isWhitespace(strName.charAt(i))) {
      i++;
    }
    return strName.substring(i);
  }

  // -mod-xxx-
  @Deprecated
  public String modUser(String modUser) {
    return ", mod_user ='" + modUser + "'";
  }
  
  public String modTime() {
    return ", mod_time =sysdate";
  }

  public String modPgm(String modPgm) {
    return ", mod_pgm ='" + modPgm + "'";
  }

  @Deprecated
  public String modSeqno(String strName) {
    if (strName.length() == 0)
      return " and nvl(mod_seqno,0)=0";
    return " and nvl(mod_seqno,0) =nvl(" + strName + ",0)";
  }

  public String setModxxx(String aModUser, String aModPgm) {
    return " mod_user ='" + aModUser + "'" + ", mod_time =sysdate" + ", mod_pgm ='" + aModPgm
        + "'" + ", mod_seqno =nvl(mod_seqno,0)+1";
  }

  public String modxxxValue(String aModUser, String aModPgm) {
    return ", '" + aModUser + "', sysdate, '" + aModPgm + "', 1";
  }

  public String modxxxSet(String aUser, String aPgm) {
    return ", mod_user ='" + aUser + "'" + ", mod_time =sysdate" + ", mod_pgm ='" + aPgm + "'"
        + ", mod_seqno = mod_seqno+1";
  }

  public String sameDept(String col, String aUser2) {
    return ", uf_same_dept(" + col + ",'" + aUser2 + "') ";
  }

  private boolean isEmpty(String strName) {
    if (strName == null)
      return true;
    return strName.trim().length() == 0;
  }

}

package com;
/** SQL條件元件
*  109/07/22  V0.00.03    Zuwei     coding standard, rename field method                   *
*  109/07/07  V0.00.02    Zuwei     coding standard, rename field method & format                   *
 * 2019-1217   JH    ++modxxx_set(xx)
 * 2019-0616:  JH    sql_modxxx
 * 2018-0926:	JH		++col(,,boolean)
 * 2018-0330:	JH		++in_pseqno
 * 2018-0327:	JH		++in_id/corp_pseqno
 * 2018-0314:	JH		++rsk_ctrlseqno
 * 2018-0314:	JH		++seq_ibm_outgoing
 * V00.00	2017-10xx	JH		initial
 * 110-01-07   V1.00.02    shiyuqi       修改无意义命名                                                                           *
 * */

public class CommSqlStr {

  public final String sqlID = "";
  public final String sqlDual = " SYSIBM.SYSDUMMY1 ";
  public final String sysYYmd = " to_char(sysdate,'yyyymmdd') ";
  public final String sysTime = " to_char(sysdate,'hh24miss') ";
  public final String sysDTime = " sysdate ";
  public final String sqlDTime = " to_char(sysdate,'yyyymmddhh24miss') ";
  public final String sqlDebitFlag = " decode(debit_flag,'Y','Y','N') as debit_flag ";
  public final String dddwUserType = "where usr_type ='4'";
  public final String whereBinno =
      " and ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9') ";
  public final String sqlModxxx = " mod_user, mod_time, mod_pgm, mod_seqno";
  // -自動編號sequence--
  public final String modSeqnoSet = " mod_seqno =nvl(mod_seqno,0)+1";
  public final String seqIbmOutgoing = " seq_ibm_outgoing.nextval ";
  // public final String seq_ecs_stop="
  // to_char(sysdate,'yy')||substr(to_char(ecs_stop.nextval,'0000000000'),4,10) ";
  public final String seqEcsStop = " substr(to_char(ecs_stop.nextval,'0000000000'),2,10) ";
  public final String seqEcsModseq = " substr(to_char(ecs_modseq.nextval,'0000000000'),2,10) ";
  public final String rskCtrlseqno =
      " to_char(sysdate,'yymm')||substr(to_char(rsk_ctrlseqno.nextval,'000000'),2) "; // "
                                                                                      // uf_rsk_ctrlseqno()
                                                                                      // ";

  public boolean isDebit(String param) {
    return param.equalsIgnoreCase("Y");
  }

  public String whereBinno(String cardNo) {
    return " and '"
        + cardNo
        + "' between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')";
  }

  public String acctKey(String param1) {
    switch (param1.trim().length()) {
      case 8:
        return param1.trim() + "000";
      case 10:
        return param1.trim() + "0";
      // default:
      // return s1;
    }
    return param1.trim();
  }

  // -in-sql->>>----------------------------------------------------------------------
  // public String in_pseqno(String col,String a_param,String cond) {
  public String inAcno(String col, String aAcctKey, String cond) {
    if (isEmpty(aAcctKey)) {
      return "";
    }
    if (isEmpty(col))
      col = "p_seqno";
    return " and "
        + col
        + " in ( select p_seqno from act_acno where 1=1"
        + col(aAcctKey, "acct_key", cond)
        + " union select p_seqno from dba_acno where 1=1"
        + col(aAcctKey, "acct_key", cond)
        + " )";
  }

  // public String in_pseqno_act(String col,String a_param,String cond) {
  public String inAcnoAct(String col, String aAcctKey, String cond) {
    if (isEmpty(aAcctKey)) {
      return "";
    }
    if (isEmpty(col))
      col = "p_seqno";
    return " and "
        + col
        + " in ( select p_seqno from act_acno where 1=1"
        + col(aAcctKey, "acct_key", cond)
        + " )";
  }

  // public String in_pseqno_dba(String col,String a_param,String cond) {
  public String inAcnoDba(String col, String aAcctKey, String cond) {
    if (isEmpty(aAcctKey)) {
      return "";
    }
    if (isEmpty(col))
      col = "p_seqno";
    return " and "
        + col
        + " in ( select p_seqno from dba_acno where 1=1"
        + col(aAcctKey, "acct_key", cond)
        + " )";
  }

  // public String in_idpseqno(String col,String param, String cond) {
  public String inIdno(String col, String aIdno, String cond) {
    if (isEmpty(aIdno)) {
      return "";
    }
    if (isEmpty(col))
      col = "id_p_seqno";

    return " and "
        + col
        + " in ( select id_p_seqno from crd_idno where 1=1"
        + col(aIdno, "id_no", cond)
        + " union select id_p_seqno from dbc_idno where 1=1"
        + col(aIdno, "id_no", cond)
        + " )";
  }

  // public String in_idpseqno_crd(String col,String param,String cond) {
  public String inIdnoCrd(String col, String aIdno, String cond) {
    if (isEmpty(aIdno)) {
      return "";
    }
    if (isEmpty(col))
      col = "id_p_seqno";

    return " and "
        + col
        + " in ( select id_p_seqno from crd_idno where 1=1"
        + col(aIdno, "id_no", cond)
        + " )";
  }

  // public String in_idpseqno_dbc(String col,String param,String cond) {
  public String inIdnoDbc(String col, String aIdno, String cond) {
    if (isEmpty(aIdno)) {
      return "";
    }
    if (isEmpty(col))
      col = "id_p_seqno";

    return " and "
        + col
        + " in ( select id_p_seqno from dbc_idno where 1=1"
        + col(aIdno, "id_no", cond)
        + " )";
  }

  // public String in_corp_pseqno(String col,String param,String cond) {
  public String inCorp(String col, String aCorpNo, String cond) {
    if (isEmpty(aCorpNo) || isEmpty(col)) {
      return "";
    }
    if (isEmpty(col))
      col = "corp_p_seqno";

    return " and "
        + col
        + " in ( select corp_p_seqno from crd_corp where 1=1"
        + col(aCorpNo, "corp_no", cond)
        + " )";
  }

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
  public String ufunc(String param1) {
    return " " + sqlID + ltrim(param1) + " ";
  }

  public String nvl(String nvl1, String nvl2) {
    return sqlID + "uf_nvl(" + nvl1 + "," + nvl2 + ") ";
  }

  static String likeReplace(String param1) {
    if (param1.indexOf("_") < 0)
      return param1;

    char[] chars = param1.toCharArray();
    String string = "";
    for (int ii = 0; ii < chars.length; ii++) {
      if (chars[ii] == '_')
        string += "\\_";
      else
        string += String.valueOf(chars[ii]);
    }
    return string;
  }

  public String col(String param, String col, boolean bRequ) {
    if (bRequ && isEmpty(param))
      return " and 1=2";
    return col(param, col);
  }

  public String col(String param1, String col, String cond) {
    String lsSql = "";

    if (isEmpty(param1) || isEmpty(col))
      return "";
    String lsCond = cond;
    if (isEmpty(cond))
      lsCond = "=";

    if (lsCond.equals("=")) {
      lsSql = " and " + col + " " + lsCond + " '" + param1 + "'";
      return lsSql;
    }
    if (lsCond.equalsIgnoreCase("like%")) {
      if (param1.indexOf("_") >= 0) {
        String str = likeReplace(param1);
        lsSql += " and " + col + " like '" + str + "%' escape '\\'";
      } else
        lsSql += " and " + col + " like '" + param1 + "%'";
    } else if (lsCond.equalsIgnoreCase("%like")) {
      if (param1.indexOf("_") >= 0) {
        String str = likeReplace(param1);
        lsSql += " and " + col + " like '%" + str + "' escape '\\'";
      } else
        lsSql += " and " + col + " like '%" + param1 + "'";
    } else if (lsCond.equalsIgnoreCase("%like%")) {
      if (param1.indexOf("_") >= 0) {
        String str = likeReplace(param1);
        lsSql += " and " + col + " like '%" + str + "%' escape '\\'";
      } else
        lsSql += " and " + col + " like '%" + param1 + "%'";
    } else {
      lsSql = " and " + col + " " + lsCond + " '" + param1 + "'";
    }

    return lsSql;
  }

  public String rownum(int num1) {
    return " fetch first " + num1 + " rows only ";
  }

  public String whereRowid(String rowid) {
    return " and rowid =x'" + rowid + "'";
  }

  public byte[] ss2rowid(String param) {
    int len = param.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
      data[i / 2] =
          (byte) ((Character.digit(param.charAt(i), 16) << 4) + Character.digit(param.charAt(i + 1), 16));
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

  public String col(String param, String col) {
    return col(param, col, "=");
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

    return " and " + col + " " + lsCond + " " + num1;
  }

  public String col(double num1, String col) {
    return col(num1, col, "=");
  }

  // -SQL=between-
  public String strend(String param1, String param2, String col) {
    return col(param1, col, ">=") + col(param2, col, "<=");
  }

  public String strend(double num1, double num2, String col) {
    return col(num1, col, ">=") + col(num2, col, "<=");
  }

  public String rtrim(String param) {
    int i = param.length() - 1;
    while (i >= 0 && Character.isWhitespace(param.charAt(i))) {
      i--;
    }
    return param.substring(0, i + 1);
  }

  public String ltrim(String param) {
    int i = 0;
    while (i < param.length() && Character.isWhitespace(param.charAt(i))) {
      i++;
    }
    return param.substring(i);
  }

  // -mod-xxx-
  public String modSeqno(String modSeqno) {
    if (modSeqno.length() == 0)
      return " and nvl(mod_seqno,0)=0";
    return " and nvl(mod_seqno,0) =nvl(" + modSeqno + ",0)";
  }

  public String setModXxx(String aModUser, String aModPgm) {
    return " mod_user ='"
        + aModUser
        + "'"
        + ", mod_time =sysdate"
        + ", mod_pgm ='"
        + aModPgm
        + "'"
        + ", mod_seqno =nvl(mod_seqno,0)+1";
  }

  public String modxxxSet(String aModUser, String aModPgm) {
    return " mod_user ='"
        + aModUser
        + "'"
        + ", mod_time =sysdate"
        + ", mod_pgm ='"
        + aModPgm
        + "'"
        + ", mod_seqno =nvl(mod_seqno,0)+1";
  }

  public String modxxxInsert(String aModUser, String aModPgm) {
    return " '" + aModUser + "', sysdate, '" + aModPgm + "', 1";
  }

  public String moduserSet(String aModUser) {
    return ", mod_user ='" + aModUser + "'";
  }

  public String modtimeSet() {
    return ", mod_time =sysdate";
  }

  public String modpgmSet(String aModPgm) {
    return ", mod_pgm ='" + aModPgm + "'";
  }

  private boolean isEmpty(String param) {
    if (param == null)
      return true;
    return param.trim().length() == 0;
  }

}

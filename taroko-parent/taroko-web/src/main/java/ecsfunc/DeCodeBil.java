package ecsfunc;
/**
 * 2020-0406:  JH    initial
 * */
public class DeCodeBil extends DeCodeBase {

public static String qrFlag(String strName) {
   return qrFlag(strName,false);
}
public static String qrFlag(String strName, boolean ddlb) {
	// ss =zzstr.decode(sql_ss("qr_flag"),"t,一維被掃(t),Q,台灣Pay收單主掃(Q),Y,台灣Pay主掃(Y),q,台灣Pay/兆豐Pay繳費(q),A,台灣Pay繳稅(A),H,兆豐Pay繳稅(H)");
   String[] aaCode= "t,Q,Y,q,A,H".split(",");
   String[] aaText ="一維被掃(t),台灣Pay收單主掃(Q),台灣Pay主掃(Y),台灣Pay/兆豐Pay繳費(q),台灣Pay繳稅(A),兆豐Pay繳稅(H)".split(",");

   if (ddlb) {
      return ddlbOption(aaCode,aaText,strName);
   }

   if (strName==null || strName.trim().length()==0) {
      return "";
   }

   return commString.decode(strName,aaCode,aaText);
}

public static String autopayIndicator(String strName) {
    String[] cardVal = {"1", "2"};
    String[] cardName = {"1.扣TTL","2.扣MP"};

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

public static String acctStatus(String strName) {
    String[] cardVal = {"1", "2", "3", "4", "5"};
    String[] cardName = {"1.正常","2.逾放","3.催收","4.呆帳","5.結清"};

    if (strName == null || strName.trim().length() == 0) {
      return "";
    }

    return commString.decode(strName, cardVal, cardName);
  }

}

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-15  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi;
/*外幣公用程式 V.2018-0510-JH
 * 
 * */
import java.math.BigDecimal;

// import java.sql.Connection;

public class CommCurr {

  // public dc_curr(Connection con1) {
  // conn =con1;
  // }
  int currAmtDp(String aCurr) {
    if (aCurr.equals("840"))
      return 2;
    return 0;
  }

  public String currCode(String asCurr) {
    if (asCurr == null)
      return "901";
    if (asCurr.trim().length() > 0)
      return asCurr.trim();

    return "901";
  }

  public boolean isTw(String asCurr) {
    if (asCurr == null || asCurr.trim().length() == 0)
      return true;

    if (asCurr.equals("901"))
      return true;
    if (asCurr.equalsIgnoreCase("TWD"))
      return true;
    return false;
  }

  public boolean amtPos(String currCode, double amt1) {
    // if (is_tw(curr_code)) {
    // return (amt1%1)==0;
    // }

    if (currAmtDp(currCode) > 0) {
      return true;
    }

    return ((amt1 * 100) % 1) == 0;
  }

  public double resetAmt(double aAmt, String aCurr) {
    // -無條件拾去>>四拾五入-
    return new BigDecimal(aAmt).setScale(currAmtDp(aCurr), BigDecimal.ROUND_HALF_UP)
        .doubleValue();
  }

  public double dc2twAmt(double destAmt, double dcDestAmt, double dcAmt) {
    if (dcDestAmt == 0 || destAmt == 0 || dcAmt == 0)
      return dcAmt;
    if (dcAmt == dcDestAmt)
      return destAmt;

    double lmAmt = (dcAmt / dcDestAmt) * destAmt;
    return (double) Math.round(lmAmt);
  }
  
  public double dc2twAmt(String curr,double destAmt, double dcDestAmt, double dcAmt) {
	  if (dcDestAmt==0 || destAmt==0 || dcAmt==0)
	      return dcAmt;
	   if (dcAmt==dcDestAmt)
	      return destAmt;
	   if (curr==null || curr.trim().length()==0 || curr.trim().equals("901"))
	      return dcAmt;

	   double lm_amt =(dcAmt / dcDestAmt) * destAmt;
	   return (double)Math.round(lm_amt);
  }
  
  public double tw2usAmt(double tAmt, double usAmt, double amt) {
    if (usAmt == 0 || tAmt == 0 || amt == 0)
      return amt;
    if (usAmt == tAmt)
      return amt;

    double lmAmt = (amt / tAmt) * usAmt;
    return new BigDecimal(lmAmt).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public double us2twAmt(double twAmt, double usAmt, double amt) {
    if (usAmt == 0 || twAmt == 0)
      return amt;
    if (usAmt == twAmt)
      return amt;

    double lmAmt = (amt / usAmt) * twAmt;
    return new BigDecimal(lmAmt).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
  }

  public double dcAmt(String aCurr, double twAmt, double dcAmt) {
    if (isTw(aCurr))
      return twAmt;

    return dcAmt;
  }

}

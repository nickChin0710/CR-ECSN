/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*                                                                            *  
******************************************************************************/
package bank.AuthIntf;

public interface FormatInterChange {
  public boolean host2Iso();

  public boolean iso2Host();

  public boolean host2Iso(String sP_IsoCommand);
}

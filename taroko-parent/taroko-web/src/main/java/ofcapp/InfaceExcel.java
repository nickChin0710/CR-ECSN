/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package ofcapp;

public interface InfaceExcel {

  public abstract void xlsPrint() throws Exception;

  abstract void logOnlineApprove() throws Exception;

}

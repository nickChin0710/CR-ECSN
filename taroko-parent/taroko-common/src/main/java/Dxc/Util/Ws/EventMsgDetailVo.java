/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*                                                                            *  
******************************************************************************/
package Dxc.Util.Ws;

public class EventMsgDetailVo {

  public String getParameters() {
    return Parameters;
  }

  public void setParameters(String parameters) {
    Parameters = parameters;
  }

  public String getContentLength() {
    return ContentLength;
  }

  public void setContentLength(String contentLength) {
    ContentLength = contentLength;
  }

  public String getContent() {
    return Content;
  }

  public void setContent(String content) {
    Content = content;
  }

  public EventMsgDetailVo() {
    // TODO Auto-generated constructor stub
  }

  private String Parameters = "";
  private String ContentLength = "";
  private String Content = "";

}

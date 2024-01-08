/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-10-14  V1.00.01  Zuwei       上傳後重命名                              *
*                                                                            *  
******************************************************************************/
package taroko.com;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javazoom.upload.*;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoUpload {
  TarokoCommon wp = null;
  public String fileName = "";
  public int rc = 1;
  public String encodingType = "UTF-8";

  public void showScreen(TarokoCommon wr) throws Exception {
    this.wp = wr;
    wp.showLogMessage("D", "showScreen", "started");
    return;
  }

  public void actionFunction(TarokoCommon wr) throws Exception {
    fileName = "";
    rc = 1;
    try {
      this.wp = wr;
      wp.request.setCharacterEncoding(encodingType);

      int maxSise = MultipartFormDataRequest.MAXCONTENTLENGTHALLOWED;
      MultipartFormDataRequest mrequ =
          new MultipartFormDataRequest(wp.request, null, maxSise,
              MultipartFormDataRequest.COSPARSER, "UTF-8");

      UploadBean upBean = new UploadBean();
      upBean.setFolderstore(TarokoParm.getInstance().getDataRoot() + "/upload");
      upBean.setOverwrite(true);

      Hashtable ht = mrequ.getFiles();
      UploadFile file = (UploadFile) ht.get("upload_file");
      // 重命名
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      String date = sdf.format(new Date());
      fileName = file.getFileName();
      int loc = fileName.lastIndexOf(".");
      if (loc >= 0) {
    	  fileName = fileName.substring(0, loc) + "_" + date + fileName.substring(loc); 
      } else {
    	  fileName = fileName + "_" + date;
      }
      file.setFileName(fileName);
      upBean.store(mrequ, "upload_file");
      fileName = file.getFileName();
      String outString = "上傳檔案　" + fileName + "　" + file.getFileSize() + " bytes 完成";
      wp.setValue("upload_message", outString, 0);
      wp.setValue("file_name", fileName, 0);
      // wp.ddd("上傳檔案 : "+
      // wp.dataRoot + "/upload/" + fileName + ", sizes= " + file.getFileSize());
    } catch (Exception ex) {
      rc = -1;
      wp.setValue("upload_message", "檔案上傳錯誤", 0);
      wp.expHandle(ex);
    }

  } // end of uploadFile

} // end of class TarokoUpload

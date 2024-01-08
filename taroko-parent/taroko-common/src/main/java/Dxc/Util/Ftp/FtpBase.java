/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-20  V1.00.01  Zuwei       updated for project coding standard      *
*                                                                            *  
******************************************************************************/
package Dxc.Util.Ftp;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.StringTokenizer;
import sun.net.TelnetInputStream;

public class FtpBase {

  public void addFtpFileName(String sPFtpFileName) {

    ProcessedFileList.add(sPFtpFileName);
  }

  public ArrayList<String> getProcessedFileList() {
    return ProcessedFileList;
  }

  private ArrayList<String> ProcessedFileList = new ArrayList<>();


  protected void clearProcessedFileList() {
    ProcessedFileList.clear();
  }

  protected boolean ifStartProcess(String sPFileName, String sPPattern) {

    boolean bLResult = false;
    String sLRealPattern = "";
    int nLStarFlagPos = sPPattern.indexOf("*");

    if (sPPattern.equals("*"))
      bLResult = true;
    else {
      // if(sP_Pattern.substring(0,1).equals("*")) { //sP_Pattern 的第一碼是"*", 譬如 "*aa"
      if (nLStarFlagPos == 0) { // sP_Pattern 的第一碼是"*", 譬如 "*aa"
        sLRealPattern = sPPattern.substring(1, sPPattern.length());

        if (sPFileName.length() >= sLRealPattern.length()) {
          if (sPFileName.substring(sPFileName.length() - sLRealPattern.length(),
              sPFileName.length()).equals(sLRealPattern)) {
            bLResult = true;
          }
        }
      }
      // else if(sP_Pattern.substring(sP_Pattern.length()-1, sP_Pattern.length()).equals("*")) {
      // //sP_Pattern 的最後一碼是"*", 譬如 "bb*"
      else if (nLStarFlagPos == sPPattern.length() - 1) { // sP_Pattern 的最後一碼是"*", 譬如 "bb*"
        sLRealPattern = sPPattern.substring(0, sPPattern.length() - 1);

        if (sPFileName.length() >= sLRealPattern.length()) {
          if (sPFileName.substring(0, sLRealPattern.length()).equals(sLRealPattern))
            bLResult = true;
        }
      } else if ((nLStarFlagPos > 0) && (nLStarFlagPos < sPPattern.length() - 1)) { // sP_Pattern
                                                                                       // 的中間有一個"*",
                                                                                       // 譬如 "a*b"

        if (sPFileName.length() >= sPPattern.length()) {
          String sLPatrernHead = sPPattern.substring(0, nLStarFlagPos);
          String sLPatrernTail = sPPattern.substring(nLStarFlagPos + 1, sPPattern.length());

          String sLFileNameHead = sPFileName.substring(0, sLPatrernHead.length());
          String sLFileNameTail =
              sPFileName.substring(sPFileName.length() - sLPatrernTail.length(),
                  sPFileName.length());

          if ((sLFileNameHead.equals(sLPatrernHead)) && (sLFileNameTail.equals(sLPatrernTail))) {
            bLResult = true;
          }

        }
      } else if (sPPattern.substring(sPPattern.length() - 1, sPPattern.length()).equals("?")) { // sP_Pattern
                                                                                                   // 的最後一碼是"?",
                                                                                                   // 譬如
                                                                                                   // "bb?"
        sLRealPattern = sPPattern.substring(0, sPPattern.length() - 1);


        if (sPFileName.indexOf(sLRealPattern) >= 0)
          bLResult = true;

      } else {
        sLRealPattern = sPPattern;
        if (sPFileName.equals(sPPattern))
          bLResult = true;

      }
    }
    return bLResult;
  }

  public FtpBase() {
    // TODO Auto-generated constructor stub
  }

  public BasicFileAttributes getFileAttributes(String sPTargetFullPathFileName) throws Exception {
    // Howard: 在 AIX 上執行時，某些檔案會出 exception...但不知道為何會如此!!

    BasicFileAttributes lBasicFileAttributes = null;
    try {
      // System.out.println("---a1---" + sP_TargetFullPathFileName);
      Path path = Paths.get(sPTargetFullPathFileName);
      // System.out.println("---a2---");
      lBasicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      // System.out.println("---a3---");


      /*
       * Path path = FileSystems.getDefault().getPath(sP_TargetFullPathFileName);
       * 
       * // function is used to view file attribute. BasicFileAttributeView view =
       * Files.getFileAttributeView (path,BasicFileAttributeView.class); L_BasicFileAttributes =
       * view.readAttributes();
       */


    } catch (Exception e) {
      // TODO: handle exception
      // System.out.println("---a4-----------" + e.getMessage());
      throw e;
    }
    // System.out.println("---a5---");
    return lBasicFileAttributes;



  }

}

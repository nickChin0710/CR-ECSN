/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
* 109-07-17  V1.00.01  Zuwei       兆豐國際商業銀行 => 合作金庫商業銀行      *
*  109-07-24  V1.00.01  Zuwei       coding standard      *
*  109-08-14  V1.00.01  Zuwei      fix code scan issue verify sql、path、輸出瀏覽器咨詢      *
*                                                                            *  
******************************************************************************/
package taroko.com;
/*2018-0316:	Jack		work-dir
 * 2017-1213: System...
 * */
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import javax.imageio.ImageIO;

import Dxc.Util.SecurityUtil;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoWaterMark {

  // TarokoCommon wp = null;
  public static void createWaterMark(TarokoCommon wp) throws Exception {

    System.setProperty("java.awt.headless", "true");
    wp.dateTime();

    String lsBmpName = wp.loginUser + ".png";
    if (wp.loginUser.length() == 0) {
      lsBmpName = "0000.png";
    }
    // String waterText = "合作金庫商業銀行 " + wp.loginUser;
    String waterText = "User: " + wp.loginUser;
    String srcWaterMarkFilename = TarokoParm.getInstance().getDataRoot() + "/waterMark/waterMark.png";
	 // verify path
    srcWaterMarkFilename = SecurityUtil.verifyPath(srcWaterMarkFilename);
    File srcWaterMark = new File(srcWaterMarkFilename);
//    File srcWaterMark = new File(wp.dataRoot + "/waterMark/waterMark.png");
    // File destWaterMark = new File(wp.dataRoot + "/work/" + ls_bmp_name);
    String destWaterMarkFilename = TarokoParm.getInstance().getWorkDir() + lsBmpName;
	 // verify path
    destWaterMarkFilename = SecurityUtil.verifyPath(destWaterMarkFilename);
    File destWaterMark = new File(destWaterMarkFilename);
//    File destWaterMark = new File(wp.workDir + lsBmpName);
    BufferedImage sourceImage = ImageIO.read(srcWaterMark);
    Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

    // initializes necessary graphic properties
    AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
    g2d.setComposite(alphaChannel);
    g2d.rotate(-0.6);
    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds(waterText, g2d);

    // calculates the coordinate where the String is painted
    int centerX = (sourceImage.getWidth() - (int) rect.getWidth()) / 2;
    int centerY = sourceImage.getHeight() / 2 + 25;

    // bank-name
    g2d.setColor(Color.GRAY);
    g2d.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 24));
    g2d.drawString(waterText, centerX - 100, centerY + 20);

    // -userID,yyyymmdd,hhmmss-
    waterText =
        wp.sysDate.substring(0, 4) + "/" + wp.sysDate.substring(4, 6) + "/"
            + wp.sysDate.substring(6) + " " + wp.sysTime.substring(0, 2) + ":"
            + wp.sysTime.substring(2, 4) + ":" + wp.sysTime.substring(4);
    g2d.setColor(Color.GRAY);
    g2d.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 24));
    // g2d.drawString(waterText, centerX - 50, centerY + 53);
    g2d.drawString(waterText, centerX - 150, centerY + 40);

    ImageIO.write(sourceImage, "png", destWaterMark);
    g2d.dispose();
    // wp.dateTime();
    // wp.showLogMessage("I","CreateWaterMark","ended");
  } // end of CreateWaterMark

} // end of class

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109/08/04  V0.00.02    Zuwei     fix code scan issue                   *
*                                                                            *  
******************************************************************************/
package taroko.com;

import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

@SuppressWarnings({"unchecked", "deprecation"})
public class TarokoMail {
  String mailServer = "";
  String portNo = "";
  String from = "";
  String to = "";
  String subject = "";
  String bodyText = "";
  String attachFile = "";

  public void sendEmail() throws Exception {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", mailServer);
    properties.put("mail.smtp.port", portNo);
    properties.put("mail.smtp.socketFactory.port", "465");
    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    Session session = Session.getDefaultInstance(properties, null);

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    subject = new String(subject.getBytes(), "BIG5");
    message.setSubject(subject, "BIG5");
    message.setSentDate(new Date());

    // Set the email message text.

    MimeBodyPart messagePart = new MimeBodyPart();
    bodyText = new String(bodyText.getBytes(), "BIG5");
    messagePart.setText(bodyText, "BIG5");

    // Set the email attachment file

    MimeBodyPart attachmentPart = new MimeBodyPart();
    FileDataSource fileDataSource = new FileDataSource(attachFile) {
      @Override
      public String getContentType() {
        return "application/octet-stream";
      }
    };
    attachmentPart.setDataHandler(new DataHandler(fileDataSource));
    attachmentPart.setFileName(attachFile);

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messagePart);
    multipart.addBodyPart(attachmentPart);
    message.setContent(multipart);
    Transport.send(message);
    return;
  }
} // end Of Class

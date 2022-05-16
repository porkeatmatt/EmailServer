package Emailserver;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class EmailUtilities {

    Properties properties;
    Session session;

    String myAccountEmail;
    String password;
    CountEmail countEmail;

    public EmailUtilities() {
        properties = new Properties();

        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        myAccountEmail = "zetHierJeGmailAdress@gmail.com";
        password = "zetHierJePaswoord";

        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        countEmail = new CountEmail(0, 0, 0, 0);
    }

    //method returns array with unread messages
    public Message[] fetchAllUnreadEmail() {
        try {
            //connecting to emails in inbox
            Store store = session.getStore();
            store.connect("pop.gmail.com", myAccountEmail, password);
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            //filtering unread emails
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            return emailFolder.search(unseenFlagTerm);

        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void processEmail() {

        try{
            Message[] unreadEmail = fetchAllUnreadEmail();
            System.out.println("er zijn " + unreadEmail.length + " ongelezen mails");


            for(int i = 0; i < unreadEmail.length; i++){
                System.out.println(getInfoEmail(unreadEmail[i]));

                String wordsEmail = unreadEmail[i].getSubject().toLowerCase() + getTextFromMessage(unreadEmail[i]).toLowerCase();

                if(wordsEmail.contains("cv")){
                    countEmail.toRecruitment += 1;
                    //forwardMail(unreadEmail[i], "matthiasandriessen1991@gmail.com");

                }else if(wordsEmail.contains("promo") || wordsEmail.contains("advertising")){
                    countEmail.toSpam += 1;

                }else if(wordsEmail.contains("proposal")){
                    countEmail.toSales += 1;
                }else{
                    countEmail.toReception += 1;
                }
            }

            System.out.println(countEmail);


        }catch (MessagingException | IOException e){
            e.printStackTrace();
        }
    }

    private String getInfoEmail(Message message) throws MessagingException, IOException {
        String subject = message.getSubject();
        StringBuilder stringBuilder = new StringBuilder();
        String from = InternetAddress.toString(message.getFrom());
        if (from != null) {
            stringBuilder.append("From: " + from);
            stringBuilder.append("\n");
        }

        if (subject != null) {
            stringBuilder.append("subject:" +subject);
            stringBuilder.append("\n");
        }
        Date sent = message.getSentDate();
        if (sent != null) {
            stringBuilder.append("Sent: " + sent);
            stringBuilder.append("\n");
        }
        String content = getTextFromMessage(message);
        if (content != null){
            stringBuilder.append("content: " + content);
        }

        stringBuilder.append("is multipart? " + message.isMimeType("multipart/*"));
        stringBuilder.append("\n");
        stringBuilder.append("---------------------------------------------------------");

        return stringBuilder.toString();
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(multipart);
        }
        return result;
    }

    public String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }

    public void forwardMail(Message message, String forwardEmail) throws MessagingException, IOException {
        Message freshMessage = new MimeMessage(session);
        freshMessage.setSubject(message.getSubject());
        freshMessage.setFrom(new InternetAddress(myAccountEmail));
        freshMessage.setText(getTextFromMessage(message));
        freshMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(forwardEmail));

        Transport.send(freshMessage);
        System.out.println("Email Forwarded!");

    }


}
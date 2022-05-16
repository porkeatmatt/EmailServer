package Emailserver;

import javax.mail.MessagingException;

public class JavaMail {
    public static void main(String[] args) throws MessagingException {
        //EmailController.sendMail("matthiasandriessen1991@gmail.com");
        EmailUtilities emailUtilities = new EmailUtilities();
        emailUtilities.processEmail();
        //emailUtilities.forwardMail();
    }
}
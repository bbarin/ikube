package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Ignore
public class MailerTest extends AbstractTest {

    @Test
    public void sendMail() throws Exception {
        // This must just pass
        IMailer mailer = new Mailer();
        mailer.setAuth("true");
        mailer.setMailHost("smtp.gmail.com");
        mailer.setPassword("1kubenotifications");
        mailer.setPort("465");
        mailer.setProtocol("pop3");
        mailer.setRecipients("ikube.notifications@gmail.com");
        mailer.setSender("ikube.notifications@gmail.com");
        mailer.setUser("ikube.notifications");

        boolean sent = mailer.sendMail("MailerTest : subject", "MailerTest : message");
        assertTrue("This mail should be sent : ", sent);
    }

}

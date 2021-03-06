/**
 * Created by dilshani on 2/25/16.
 */
import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

// SOAP Message
// <m:placeOrder xmlns:m="http://services.samples">
// <m:order>
// <m:price>82.9788684380384</m:price>
// <m:quantity>17215</m:quantity>
// <m:symbol>IBM</m:symbol>
// </m:order>
// </m:placeOrder>

public class SOAPPublisher {
    private static final Logger log = Logger.getLogger(SOAPPublisher.class.getName());

    // Set up all the default values
    private static final String param = "IBM";
    // with header for inbounds
    private static final String MESSAGE_WITH_HEADER =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "   <soapenv:Header/>\n" +
                    "<soapenv:Body>\n" +
                    "<m:placeOrder xmlns:m=\"http://services.samples\">\n" +
                    "    <m:order>\n" +
                    "        <m:price>" +
                    getRandom(100, 0.9, true) +
                    "</m:price>\n" +
                    "        <m:quantity>" +
                    (int) getRandom(10000, 1.0, true) +
                    "</m:quantity>\n" +
                    "        <m:symbol>" +
                    param +
                    "</m:symbol>\n" +
                    "    </m:order>\n" +
                    "</m:placeOrder>" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
    private static final String DEFAULT_CONNECTION_FACTORY = "QueueConnectionFactory";
    private static final String DEFAULT_DESTINATION = "queue/mySampleQueue";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";
    private static final String PROVIDER_URL = "jnp://192.168.48.146:1099";

    public static void main(String[] args) {

        Context namingContext = null;

        try {

            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
            namingContext = new InitialContext(env);


            // Perform the JNDI lookups
            String connectionFactoryString =
                    System.getProperty("connection.factory",
                            DEFAULT_CONNECTION_FACTORY);
            log.info("Attempting to acquire connection factory \"" + connectionFactoryString + "\"");
            ConnectionFactory connectionFactory =
                    (ConnectionFactory) namingContext.lookup(connectionFactoryString);
            log.info("Found connection factory \"" + connectionFactoryString + "\" in JNDI");

            String destinationString = System.getProperty("destination", DEFAULT_DESTINATION);
            log.info("Attempting to acquire destination \"" + destinationString + "\"");
            Destination destination = (Destination) namingContext.lookup(destinationString);
            log.info("Found destination \"" + destinationString + "\" in JNDI");

            // String content = System.getProperty("message.content",
            // DEFAULT_MESSAGE);
            String content = System.getProperty("message.content", MESSAGE_WITH_HEADER);

            try (JMSContext context = connectionFactory.createContext()) {
                log.info("Sending  message");
                // Send the message
                for(int i=0;i<5;i++) {
                    context.createProducer().send(destination, content);
                }
            }

        } catch (NamingException e) {
            log.severe(e.getMessage());
        } finally {
            if (namingContext != null) {
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    log.severe(e.getMessage());
                }
            }
        }
    }

    private static double getRandom(double base, double varience, boolean onlypositive) {
        double rand = Math.random();
        return (base + (rand > 0.5 ? 1 : -1) * varience * base * rand) *
                (onlypositive ? 1 : rand > 0.5 ? 1 : -1);
    }
}
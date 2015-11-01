package company.tothepoint.nao.service;

import com.aldebaran.qi.Application;
import com.aldebaran.qi.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class RobotSession {
    private static final Logger LOG = LoggerFactory.getLogger(RobotSession.class);

    private String robotUri = "tcp://192.168.0.222:9559";
    private String[] args = new String[0];

    private Application application;

    public RobotSession() {
        LOG.warn("Robot session started!");
        application = new Application(args, robotUri);
        application.start();
    }

    public Session getSession() {
        if (application.session().isConnected())
            return application.session();
        else {
            application.start();
            return application.session();
        }

    }
}

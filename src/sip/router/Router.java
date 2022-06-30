package sip.router;

import sip.SipRequest;
import sip.SipResponse;
import sip.server.SipServer;
import sip.controller.AbstractController;
import sip.security.Security;

import java.lang.reflect.Method;

public class Router {
    private String config;
    private final SipServer sipServer;

    public Router(SipServer server) {
        this.sipServer = server;
    }

    public SipResponse process(String rawRequest) {
        SipRequest request = SipRequest.parse(rawRequest);

        return this.process(request);
    }

    public SipResponse process(SipRequest request) {
        if (request.getMethod().equalsIgnoreCase("register") || sipServer.getSecurity().process(request)) {
            String controllerName = "sip.controller.IndexController";
            try {
                AbstractController controller = (AbstractController) Class.forName(controllerName).newInstance();
                Method method = controller.getClass().getDeclaredMethod(request.getMethod(), SipServer.class, SipRequest.class, Security.class);

                return (SipResponse) method.invoke(controller, this.sipServer, request, sipServer.getSecurity());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version)).setStatusCode(500).setReason("NO_ROUTE");
            }
        }

        return SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version)).setStatusCode(401).setReason("Unauthorized");
    }
}

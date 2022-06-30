package sip.controller;

import sip.*;
import sip.security.Security;
import sip.security.User;
import sip.server.SipServer;

public class IndexController extends AbstractController {
    public IndexController() {
    }

    public SipResponse REGISTER(SipServer sipServer, SipRequest request, Security security) {
        SipResponse response = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        response
                .setUriTransport(SipServer.protocol)
                .addVia(
                        request.getViaSipVersion(),
                        request.getViaTransport(),
                        request.getViaAddress(),
                        request.getViaBranch()
                )
                .addTo(
                        request.getToName(),
                        request.getToAddress(),
                        request.getToTransport(),
                        null
                )
                .addFrom(
                        request.getFromName(),
                        request.getFromAddress(),
                        request.getFromTransport(),
                        request.getFromTag()
                )
                .addCSeq(
                        request.getcSecNumber(),
                        request.getMethod()
                )
                .addCallId(request.getCallId());

        if (request.getContactExpires() == 0) {
            security.removeUser(request);
            response
                    .setStatusCode(200)
                    .addContact(request.getContactName(),request.getContactAddress(),request.getContactTransport(),0)
                    .setReason("OK");

            return response;
        }

        if (request.getAuthUsername() == null) {
            response
                    .setStatusCode(401)
                    .setReason("Unauthorized")
                    .addAuth("Digest", "test", "asd", "MD5");
        } else {
            response
                    .setReason("OK")
                    .setStatusCode(200)
                    .addContact(request.getContactName(), request.getContactAddress(), request.getContactTransport(), 3600);

            security.addUser(request);
        }

        return response;
    }

    public SipResponse INVITE(SipServer sipServer, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .addCallId(request.getCallId())
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null);

        if (!security.hasUser(request.getToName())) {
            sipResponse
                    .setStatusCode(404)
                    .setReason("User not found");
        } else {
            User user = security.getUser(request.getToName());
            sipServer.send(user, request.toString());

            sipResponse
                    .setStatusCode(100)
                    .setReason("Trying");
        }

        return sipResponse;
    }

    public SipResponse RINGING(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .setStatusCode(180)
                .setReason("RINGING")
                .addCallId(request.getCallId())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                //TODO DO NOT FORGET TO PUT toTag ! According to standard - it is mandatory p.s Required only during call session, register method does not require it
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null)
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch());

        if (security.getUser(request).getLogin().equals(request.getToName())) {
            server.send(security.getUser(request.getFromName()), request.toString());
        } else {
            server.send(security.getUser(request.getToName()), request.toString());
        }

        return null;
    }

    public SipResponse TRYING(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .setStatusCode(100)
                .setReason("TRYING")
                .addCallId(request.getCallId())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null)
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch());

        return sipResponse;
    }

    public SipResponse ACK(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .setStatusCode(200)
                .setReason("OK")
                .addCallId(request.getCallId())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null)
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch());

        if (security.getUser(request).getLogin().equals(request.getToName())) {
            server.send(security.getUser(request.getFromName()), request.toString());
        } else {
            server.send(security.getUser(request.getToName()), request.toString());
        }

        return null;//sipResponse;
    }

    public SipResponse BYE(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .setStatusCode(200)
                .setReason("OK")
                .addCallId(request.getCallId())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                //TODO DO NOT FORGET TO PUT toTag ! According to standard - it is mandatory p.s Required only during call session, register method does not require it
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null)
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch());

        if (security.getUser(request).getLogin().equals(request.getToName())) {
            server.send(security.getUser(request.getFromName()), request.toString());
        } else {
            server.send(security.getUser(request.getToName()), request.toString());
        }

        return null;
    }

    public SipResponse CANCEL(SipServer server, SipRequest request, Security security) {
        if (security.getUser(request).getLogin().equals(request.getToName())) {
            server.send(security.getUser(request.getFromName()), request.toString());
        } else {
            server.send(security.getUser(request.getToName()), request.toString());
        }

        return null;//sipResponse;
    }

    public SipResponse Decline(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .addCallId(request.getCallId())
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null);

        if (!security.hasUser(request.getFromName())) {
            sipResponse
                    .setStatusCode(404)
                    .setReason("User not found");
        } else {
            if (security.getUser(request).getLogin().equals(request.getToName())) {
                server.send(security.getUser(request.getFromName()), request.toString());
            } else {
                server.send(security.getUser(request.getToName()), request.toString());
            }

            sipResponse
                    .setStatusCode(200)
                    .setReason("ACK");
        }

        server.send(security.getUser(request), sipResponse.toString());

        return null;
    }

    public SipResponse SUBSCRIBE(SipServer server, SipRequest request, Security security) {
        SipResponse sipResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));

        sipResponse
                .setUriTransport(request.getUriTransport())
                .setStatusCode(200)
                .setReason("OK")
                .addCallId(request.getCallId())
                .addCSeq(request.getcSecNumber(), request.getcSecMethod())
                .addFrom(request.getFromName(), request.getFromAddress(), request.getFromTransport(), request.getFromTag())
                .addTo(request.getToName(), request.getToAddress(), request.getToTransport(), null)
                .addVia(request.getViaSipVersion(), request.getViaTransport(), request.getViaAddress(), request.getViaBranch());

        return sipResponse;
    }
}

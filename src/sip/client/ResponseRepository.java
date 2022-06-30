package sip.client;

import sip.AbstractSipStructure;
import sip.SipResponse;

public class ResponseRepository {
    public static SipResponse trying(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipResponse response = SipResponse.create("Experimental Sip Client 0.0.1");

        response
                .setStatusCode(100)
                .setReason("Trying")
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.ACK.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return response;
    }

    public static SipResponse ringing(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipResponse response = SipResponse.create("Experimental Sip Client 0.0.1");

        response
                .setStatusCode(180)
                .setReason("Ringing")
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.ACK.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return response;
    }

    public static SipResponse decline(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipResponse response = SipResponse.create("Experimental Sip Client 0.0.1");

        response
                .setStatusCode(630)
                .setReason("Decline")
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.ACK.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return response;
    }

    public static SipResponse ok(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipResponse response = SipResponse.create("Experimental Sip Client 0.0.1");

        response
                .setStatusCode(200)
                .setReason("OK")
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.ACK.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return response;
    }

    public static SipResponse dontExist(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipResponse response = SipResponse.create(SipClient.userAgent);

        response
                .setStatusCode(481)
                .setReason("Dialog does not exist")
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return response;
    }

}

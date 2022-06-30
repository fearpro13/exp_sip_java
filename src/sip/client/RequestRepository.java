package sip.client;

import sip.AbstractSipStructure;
import sip.SipRequest;

public class RequestRepository {
    public static SipRequest register(String hostIp,int hostPort,String login,String clientIp,int clientPort,String fromTag,String branch,String protocol){
        //since 2022-June-16
        SipRequest request = SipRequest.create("Experimental Sip Client 0.0.1");

        request
                .setMethod(AbstractSipStructure.METHODS.REGISTER.name())
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(login, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(login,hostIp,protocol,fromTag)
                .addTo(login,hostIp,protocol,null)
                .addCSeq(1,AbstractSipStructure.METHODS.REGISTER.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods)
                .addAuth("Digest",login,"test","NOTHING_HERE",request.getUriAddress(),"NOTHING_HERE");

        return request;
    }

    public static SipRequest invite(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipRequest request = SipRequest.create("Experimental Sip Client 0.0.1");

//        String body = "v=0\n" +
//                "o=Z 0 3767658 IN IP4 10.0.43.19\n" +
//                "s=Z\n" +
//                "c=IN IP4 10.0.43.19\n" +
//                "t=0 0\n" +
//                "m=audio 43245 RTP/AVP 106 9 98 101 0 8 3\n" +
//                "a=rtpmap:106 opus/48000/2\n" +
//                "a=fmtp:106 sprop-maxcapturerate=16000; minptime=20; useinbandfec=1\n" +
//                "a=rtpmap:98 telephone-event/48000\n" +
//                "a=fmtp:98 0-16\n" +
//                "a=rtpmap:101 telephone-event/8000\n" +
//                "a=fmtp:101 0-16\n" +
//                "a=sendrecv";
        String body = "";

        request
                .setMethod(AbstractSipStructure.METHODS.INVITE.name())
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriName(toLogin)
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.INVITE.name())
                .addCallId(SipClient.callId)
                .setBody(body)
                .addAllowed(AbstractSipStructure.allowedMethods);

        return request;
    }

    public static SipRequest ack(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipRequest request = SipRequest.create("Experimental Sip Client 0.0.1");

        request
                .setMethod(AbstractSipStructure.METHODS.ACK.name())
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

        return request;
    }

    public static SipRequest bye(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipRequest request = SipRequest.create("Experimental Sip Client 0.0.1");

        request
                .setMethod(AbstractSipStructure.METHODS.BYE.name())
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.BYE.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return request;
    }

    public static SipRequest cancel(String fromLogin,String toLogin,String hostIp,int hostPort,String clientIp,int clientPort,String branch,String fromTag,String toTag,String protocol){
        SipRequest request = SipRequest.create("Experimental Sip Client 0.0.1");

        request
                .setMethod(AbstractSipStructure.METHODS.CANCEL.name())
                .setUriName(toLogin)
                .setUriAddress(String.format("%s:%d", hostIp,hostPort))
                .setUriTransport(protocol)
                .addVia("2.0",protocol, String.format("%s:%d", clientIp,clientPort),branch)
                .addContact(fromLogin, String.format("%s:%d", clientIp,clientPort),protocol,-1)
                .addFrom(fromLogin,hostIp,protocol,fromTag)
                .addTo(toLogin,hostIp,protocol,toTag)
                .addCSeq(1,AbstractSipStructure.METHODS.CANCEL.name())
                .addCallId(SipClient.callId)
                .setBody("")
                .addAllowed(AbstractSipStructure.allowedMethods);

        return request;
    }
}

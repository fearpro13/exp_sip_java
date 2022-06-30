package sip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

abstract public class AbstractSipStructure<T extends AbstractSipStructure<T>> {
    public static final String[] allowedMethods = new String[]{
            "REGISTER",
            "INVITE",
            "ACK",
            "CANCEL",
            "BYE",
            "OPTIONS",
            "SUBSCRIBE"
    };

    public enum METHODS {
        REGISTER,
        INVITE,
        ACK,
        CANCEL,
        BYE,
        OPTIONS,
        SUBSCRIBE
    }

    protected String remoteIp;
    protected int remotePort;

    protected String method;
    protected String uriName;
    protected String uriAddress;
    protected String uriTransport;
    protected String sipVersion;

    protected String viaSipVersion;
    protected String viaTransport;
    protected String viaAddress;
    protected String viaBranch;

    protected int maxForwards;

    protected String contactName;
    protected String contactAddress;
    protected String contactTransport;
    protected int contactExpires = -1;

    protected String toName;
    protected String toAddress;
    protected String toTransport;
    protected String toTag;

    protected String fromName;
    protected String fromAddress;
    protected String fromTransport;
    protected String fromTag;

    protected String callId;

    protected int cSecNumber;
    protected String cSecMethod;

    protected String authType;
    protected String authUsername;
    protected String authRealm;
    protected String authNonce;
    protected String authUri;
    protected String authResponse;

    protected int expires;

    protected String[] allowed;

    protected String[] supported;

    protected String userAgent;

    protected String[] allowedEvents;

    protected String body;

    protected int contentLength = 0;

    protected final LinkedHashMap<String, String> headers = new LinkedHashMap<>();

    public String getMethod() {
        return this.method;
    }

    public T setMethod(String method) {
        this.method = method;

        return (T) this;
    }

    public String getUriName() {
        return uriName;
    }

    public T setUriName(String uriName) {
        this.uriName = uriName;

        return (T) this;
    }

    public String getUriAddress() {
        return uriAddress;
    }

    public T setUriAddress(String uriAddress) {
        this.uriAddress = uriAddress;

        return (T) this;
    }

    public String getUriTransport() {
        return uriTransport;
    }

    public T setUriTransport(String uriTransport) {
        this.uriTransport = uriTransport;

        return (T) this;
    }

    public String getViaSipVersion() {
        return viaSipVersion;
    }

    public T setViaSipVersion(String viaSipVersion) {
        this.viaSipVersion = viaSipVersion;

        return (T) this;
    }

    public T addVia(
            String viaSipVersion,
            String viaTransport,
            String viaAddress,
            String viaBranch) {
        this
                .setViaSipVersion(viaSipVersion)
                .setViaTransport(viaTransport)
                .setViaAddress(viaAddress)
                .setViaBranch(viaBranch);

        String headerValue;
        if (viaBranch != null) {
            headerValue = String.format(
                    "SIP/%s/%s %s;branch=%s;rport",
                    viaSipVersion,
                    viaTransport,
                    viaAddress,
                    viaBranch
            );
        } else {
            headerValue = String.format(
                    "SIP/%s/%s %s;rport",
                    viaSipVersion,
                    viaTransport,
                    viaAddress
            );
        }

        this.addHeader("Via", headerValue);


        return (T) this;
    }

    public String getViaTransport() {
        return viaTransport;
    }

    public T setViaTransport(String viaTransport) {
        this.viaTransport = viaTransport;

        return (T) this;
    }

    public T setSipVersion(String sipVersion) {
        this.sipVersion = sipVersion;

        return (T) this;
    }

    public String getSipVersion() {
        return this.sipVersion;
    }

    public T setViaAddress(String address) {
        this.viaAddress = address;
        return (T) this;
    }

    public String getViaAddress() {
        return this.viaAddress;
    }

    public String getViaBranch() {
        return viaBranch;
    }

    public T setViaBranch(String viaBranch) {
        this.viaBranch = viaBranch;

        return (T) this;
    }

    public T setMaxForwards(int maxForwards) {
        this.maxForwards = maxForwards;

        return (T) this;
    }

    public T addMaxForwards(int maxForwards) {
        this
                .setMaxForwards(maxForwards)
                .addHeader("Max-Forwards", String.valueOf(maxForwards));

        return (T) this;
    }

    public int getMaxForwards() {
        return this.maxForwards;
    }

    public T addContact(String contactName, String contactAddress, String contactTransport, int contactExpires) {
        String headerValue;

        if(contactTransport != null){
            this.setContactTransport(contactTransport);
            headerValue = String.format(
                    "<sip:%s@%s;transport=%s>",
                    contactName,
                    contactAddress,
                    contactTransport);
        }else{
            headerValue = String.format(
                    "<sip:%s@%s>",
                    contactName,
                    contactAddress);
        }

        if (contactExpires != -1) {
            this.setContactExpires(contactExpires);
            headerValue += String.format(";expires=%d",contactExpires);
        }

        this
                .setContactName(contactName)
                .setContactAddress(contactAddress)
                .addHeader(
                        "Contact", headerValue
                );

        return (T) this;
    }

    public String getContactName() {
        return contactName;
    }

    public T setContactName(String contactName) {
        this.contactName = contactName;

        return (T) this;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public T setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;

        return (T) this;
    }

    public String getContactTransport() {
        return contactTransport;
    }

    public T setContactTransport(String contactTransport) {
        this.contactTransport = contactTransport;

        return (T) this;
    }

    public int getContactExpires() {
        return contactExpires;
    }

    public T setContactExpires(int contactExpires) {
        this.contactExpires = contactExpires;

        return (T) this;
    }

    public T addTo(String toName, String toAddress, String toTransport, String toTag) {
        String headerValue;

        this
                .setToName(toName)
                .setToAddress(toAddress)
                .setToTransport(toTransport)
                .setToTag(toTag);

        if (toTransport != null) {
            headerValue = String.format(
                    "<sip:%s@%s;transport=%s>",
                    toName,
                    toAddress,
                    toTransport
            );
        } else {
            headerValue = String.format(
                    "<sip:%s@%s>",
                    toName,
                    toAddress
            );
        }

        if (toTag != null) {
            headerValue += String.format(";tag=%s", toTag);
        }

        this.addHeader("To", headerValue);

        return (T) this;
    }

    public String getToName() {
        return toName;
    }

    public T setToName(String toName) {
        this.toName = toName;

        return (T) this;
    }

    public String getToAddress() {
        return toAddress;
    }

    public T setToAddress(String toAddress) {
        this.toAddress = toAddress;

        return (T) this;
    }

    public String getToTransport() {
        return toTransport;
    }

    public T setToTransport(String toTransport) {
        this.toTransport = toTransport;

        return (T) this;
    }

    public String getToTag() {
        return toTag;
    }

    public T setToTag(String toTag) {
        this.toTag = toTag;

        return (T) this;
    }

    public T addFrom(
            String fromName,
            String fromAddress,
            String fromTransport,
            String fromTag) {
        this
                .setFromName(fromName)
                .setFromAddress(fromAddress)
                .setFromTransport(fromTransport)
                .setFromTag(fromTag);

        String headerValue;

        if (fromTransport != null) {
            this.setFromTransport(fromTransport);
            headerValue = String.format(
                    "<sip:%s@%s;transport=%s>",
                    fromName,
                    fromAddress,
                    fromTransport
            );
        } else {
            headerValue =
                    String.format(
                            "<sip:%s@%s>",
                            fromName,
                            fromAddress
                    );
        }

        if (fromTag != null) {
            this.setFromTag(fromTag);
            headerValue += String.format(";tag=%s", fromTag);
        }

        this.addHeader("From", headerValue);

        return (T) this;
    }

    public String getFromName() {
        return fromName;
    }

    public T setFromName(String fromName) {
        this.fromName = fromName;

        return (T) this;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public T setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;

        return (T) this;
    }

    public String getFromTransport() {
        return fromTransport;
    }

    public T setFromTransport(String fromTransport) {
        this.fromTransport = fromTransport;

        return (T) this;
    }

    public String getFromTag() {
        return fromTag;
    }

    public T setFromTag(String fromTag) {
        this.fromTag = fromTag;

        return (T) this;
    }

    public T addCallId(String callId) {
        this.setCallId(callId).addHeader("Call-ID", callId);
        return (T) this;
    }

    public String getCallId() {
        return this.callId;
    }

    public T setCallId(String callId) {
        this.callId = callId;

        return (T) this;
    }

    public T addCSeq(int cSecNumber, String cSecMethod) {
        this.
                setcSecNumber(cSecNumber)
                .setcSecMethod(cSecMethod)
                .addHeader(
                        "CSeq",
                        String.format("%d %s", cSecNumber, cSecMethod));

        return (T) this;
    }

    public int getcSecNumber() {
        return cSecNumber;
    }

    public T setcSecNumber(int cSecNumber) {
        this.cSecNumber = cSecNumber;

        return (T) this;
    }

    public String getcSecMethod() {
        return cSecMethod;
    }

    public T setcSecMethod(String cSecMethod) {
        this.cSecMethod = cSecMethod;

        return (T) this;
    }

    public T addAuth(
            String authType,
            String authUsername,
            String authRealm,
            String authNonce,
            String authUri,
            String authResponse) {
        this
                .setAuthType(authType)
                .setAuthUsername(authUsername)
                .setAuthRealm(authRealm)
                .setAuthNonce(authNonce)
                .setAuthUri(authUri)
                .setAuthResponse(authResponse)
                .addHeader(
                        "Authorization",
                        String.format("%s username=\"%s\",realm=\"%s\",nonce=\"%s\",uri=\"%s\",response=\"%s\"",
                                authRealm,
                                authUsername,
                                authRealm,
                                authNonce,
                                authUri,
                                authResponse)
                );

        return (T) this;
    }

    public String getAuthType() {
        return authType;
    }

    public T setAuthType(String authType) {
        this.authType = authType;

        return (T) this;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public T setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
        return (T) this;
    }

    public String getAuthRealm() {
        return authRealm;
    }

    public T setAuthRealm(String authRealm) {
        this.authRealm = authRealm;
        return (T) this;
    }

    public String getAuthNonce() {
        return authNonce;
    }

    public T setAuthNonce(String authNonce) {
        this.authNonce = authNonce;
        return (T) this;
    }

    public String getAuthUri() {
        return authUri;
    }

    public T setAuthUri(String authUri) {
        this.authUri = authUri;
        return (T) this;
    }

    public String getAuthResponse() {
        return authResponse;
    }

    public T setAuthResponse(String authResponse) {
        this.authResponse = authResponse;
        return (T) this;
    }

    public int getExpires() {
        return expires;
    }

    public T addExpires(int expires) {
        this.setExpires(expires).addHeader("Expires", String.valueOf(expires));

        return (T) this;
    }

    public T setExpires(int expires) {
        this.expires = expires;

        return (T) this;
    }

    public T addAllowed(String[] allowed) {
        this.setAllowed(allowed).addHeader(
                "Allow",
                String.join(",", allowed));

        return (T) this;
    }

    public String[] getAllowed() {
        return allowed;
    }

    public T setAllowed(String[] allowed) {
        this.allowed = allowed;

        return (T) this;
    }

    public T addSupported(String[] supported) {
        this
                .setSupported(supported)
                .addHeader(
                        "Supported",
                        String.join(",", supported));

        return (T) this;
    }

    public String[] getSupported() {
        return supported;
    }

    public T setSupported(String[] supported) {
        this.supported = supported;

        return (T) this;
    }

    public T addUserAgent(String userAgent) {
        this.setUserAgent(userAgent).addHeader("User-Agent", userAgent);
        return (T) this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public T setUserAgent(String userAgent) {
        this.userAgent = userAgent;

        return (T) this;
    }

    public T addAllowEvents(String[] allowedEvents) {
        this.setAllowedEvents(allowedEvents).addHeader(
                "Allow-Events",
                String.join(",", allowedEvents));

        return (T) this;
    }

    public String[] getAllowedEvents() {
        return allowedEvents;
    }

    public T setAllowedEvents(String[] allowedEvents) {
        this.allowedEvents = allowedEvents;

        return (T) this;
    }

    public T addContentLength(int contentLength) {
        this.setContentLength(contentLength).addHeader(
                "Content-Length",
                String.valueOf(contentLength)
        );

        return (T) this;
    }

    public int getContentLength() {
        return contentLength;
    }

    private T setContentLength(int contentLength) {
        this.contentLength = contentLength;

        return (T) this;
    }

    public String getBody() {
        return this.body;
    }

    public T setBody(String body) {
        this.body = body;
        this.addContentLength(body.length());

        return (T) this;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public T setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;

        return (T) this;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public T setRemotePort(int remotePort) {
        this.remotePort = remotePort;

        return (T) this;
    }

    public T addHeader(String name, String value) {
        this.headers.put(name, value);

        return (T) this;
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public static boolean hasParameter(String headerRow, String parameterName) {
        return headerRow.contains(parameterName + "=");
    }

    public static String getParameterValue(String headerRow, String parameterName) {
        String formattedParameterName = parameterName + "=";

        if (!headerRow.contains(formattedParameterName)) {
            return null;
        }

        int beginPos = headerRow.indexOf(formattedParameterName);
        int endPos = headerRow.indexOf(";", beginPos);

        if (endPos == -1) {
            endPos = headerRow.length();
        }

        if (beginPos == -1) {
            return null;
        }

        return headerRow.substring(beginPos + formattedParameterName.length(), endPos);
    }

    public static boolean isRequest(String message) {
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(message.split("\r\n")));

        if (lines.isEmpty() || lines.get(0) == null || lines.get(0).split(" ").length < 2) {
            return false;
        }

        String[] firstLineSplit = lines.get(0).split(" ");
        String[] sipInfoSplit = firstLineSplit[2].split("/");

        return sipInfoSplit.length == 2 && sipInfoSplit[0].equalsIgnoreCase("sip");
    }

    public static boolean isResponse(String message) {
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(message.split("\r\n")));

        if (lines.isEmpty() || lines.get(0) == null || lines.get(0).split(" ").length < 3) {
            return false;
        }

        String[] firstLineSplit = lines.get(0).split(" ");
        String[] sipInfoSplit = firstLineSplit[0].split("/");

        return sipInfoSplit.length == 2 && sipInfoSplit[0].equalsIgnoreCase("sip");
    }
}

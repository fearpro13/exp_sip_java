package sip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class SipResponse extends AbstractSipStructure<SipResponse> {
    private int statusCode;
    private String reason;

    private String viaReceived;

    private String toTag;

    private String authAlgorithm;

    public SipResponse() {
    }

    public static SipResponse create(String userAgent) {
        SipResponse response = new SipResponse();
        response
                .setSipVersion("2.0")
                .setStatusCode(200)
                .addUserAgent(userAgent)
                .addAllowed(AbstractSipStructure.allowedMethods)
                .setBody("");

        return response;
    }

    public static SipResponse parse(String rawResponse) {
        SipResponse response = new SipResponse();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(rawResponse.split("\r\n")));

        String[] firstLineSplit = lines.get(0).split(" ");
        response
                .setSipVersion(firstLineSplit[0].split("/")[1])
                .setStatusCode(Integer.parseInt(firstLineSplit[1]))
                .setReason(firstLineSplit[2]);

        response.setBody("");

        lines.remove(0);
        for (String line : lines) {
            parseLine(line, response);
            if (line.equals("")) {
                break;
            }
        }

        return response;
    }

    private static void parseLine(String line, SipResponse response) {
        String[] addressInfoSplit;
        String formatLine;
        String[] lineSplit;

        int firstColonPos = line.indexOf(":");
        String headerName = line.substring(0, firstColonPos);
        String headerValue = line.substring(firstColonPos + 1).trim();
        String lowerHeaderName = headerName.toLowerCase().trim();
        switch (lowerHeaderName) {
            case "via":
                String[] valueSplit = headerValue.split(" ");
                String protocolInfo = valueSplit[0].trim();
                String addressInfo = valueSplit[1].trim();

                String[] protocolInfoSplit = protocolInfo.split("/");
                addressInfoSplit = addressInfo.split(";");

                String viaBranch = null;
                if (hasParameter(valueSplit[1], "branch")) {
                    viaBranch = getParameterValue(valueSplit[1], "branch");
                }

                response.addVia(
                        protocolInfoSplit[1],
                        protocolInfoSplit[2],
                        addressInfoSplit[0],
                        viaBranch
                );

                break;
            case "max-forwards":
                response.addMaxForwards(Integer.parseInt(headerValue));
                break;
            case "contact":
                formatLine = headerValue
                        .replaceAll("<", "")
                        .replaceAll(">", "");

                lineSplit = formatLine.split(";");
                addressInfoSplit = lineSplit[0].split("@");

                String transport = null;
                if (hasParameter(formatLine, "transport")) {
                    transport = getParameterValue(formatLine, "transport");
                }

                int expires = -1;
                if (hasParameter(formatLine, "expires")) {
                    expires = Integer.parseInt(getParameterValue(formatLine, "expires"));
                }

                response.addContact(
                        addressInfoSplit[0].split(":")[1],
                        addressInfoSplit[1],
                        transport,
                        expires
                );
                break;
            case "to":
                formatLine = headerValue
                        .replaceAll("<", "")
                        .replaceAll(">", "");

                lineSplit = formatLine.split(";");
                addressInfoSplit = lineSplit[0].split("@");

                String toTransport = null;
                if (hasParameter(formatLine, "transport")) {
                    toTransport = getParameterValue(formatLine, "transport");
                }

                String toTag = null;
                if (hasParameter(formatLine, "tag")) {
                    toTag = getParameterValue(formatLine, "tag");
                }

                response.addTo(
                        addressInfoSplit[0].split(":")[1],
                        addressInfoSplit[1],
                        toTransport,
                        toTag
                );
                break;
            case "from":
                formatLine = headerValue
                        .replaceAll("<", "")
                        .replaceAll(">", "");

                lineSplit = formatLine.split(";");
                addressInfoSplit = lineSplit[0].split("@");

                String fromTransport = null;
                String fromTag = null;

                if (hasParameter(formatLine, "transport")) {
                    fromTransport = getParameterValue(formatLine, "transport");
                }

                if (hasParameter(formatLine, "tag")) {
                    fromTag = getParameterValue(formatLine, "tag");
                }

                response.addFrom(
                        addressInfoSplit[0].split(":")[1],
                        addressInfoSplit[1],
                        fromTransport,
                        fromTag
                );
                break;
            case "call-id":
                response.addCallId(headerValue);
                break;
            case "cseq":
                lineSplit = headerValue.split(" ");

                response.addCSeq(Integer.parseInt(lineSplit[0]), lineSplit[1]);
                break;
            case "expires":
                response.addExpires(Integer.parseInt(headerValue));
                break;
            case "allow":
                response.addAllowed(headerValue.split(","));
                break;
            case "supported":
                response.addSupported(headerValue.split(","));
                break;
            case "user-agent":
                response.addUserAgent(headerValue);
                break;
            case "allow-events":
                response.addAllowEvents(headerValue.split(","));
                break;
            case "content-length":
                response.addContentLength(Integer.parseInt(headerValue));
                break;
        }
    }

    private static void parseBody(String body, SipResponse response) {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public SipResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;

        return this;
    }

    public String getReason() {
        return reason;
    }

    public SipResponse setReason(String reason) {
        this.reason = reason;

        return this;
    }

    public String getViaReceived() {
        return viaReceived;
    }

    public SipResponse setViaReceived(String viaReceived) {
        this.viaReceived = viaReceived;

        return this;
    }

    public <T> SipResponse addAuth(
            String authType,
            String authRealm,
            String authNonce,
            String authAlgorithm) {
        this
                .setAuthType(authType)
                .setAuthRealm(authRealm)
                .setAuthNonce(authNonce)
                .setAuthAlgorithm(authAlgorithm)
                .addHeader("WWW-Authenticate",
                        String.format("%s realm=\"%s\",nonce=\"%s\",algorithm=\"%s\"",
                                authType,
                                authRealm,
                                authNonce,
                                authAlgorithm
                        ));

        return this;
    }

    public String getAuthAlgorithm() {
        return authAlgorithm;
    }

    public SipResponse setAuthAlgorithm(String authAlgorithm) {
        this.authAlgorithm = authAlgorithm;
        return this;
    }

    @Override
    public String toString() {
        String response = "";
        String responseLine = String.format(
                "SIP/%s %d %s\r\n",
                getSipVersion(),
                getStatusCode(),
                getReason()
        );

        this.addContentLength(this.body.length());

        final AtomicReference<String> headers = new AtomicReference<>("");
        this.headers.forEach((name, value) -> headers.set(headers + String.format("%s: %s\r\n", name, value)));

        response += responseLine;
        response += headers;
        response += "\r\n";
        response += getBody();

        return response;
    }
}

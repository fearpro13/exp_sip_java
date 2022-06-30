package sip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class SipRequest extends AbstractSipStructure<SipRequest> {

    public SipRequest() {
    }

    public static SipRequest create(String userAgent) {
        SipRequest request = new SipRequest();
        request
                .setSipVersion("2.0")
                .addUserAgent(userAgent)
                .addAllowed(AbstractSipStructure.allowedMethods)
                .addMaxForwards(70)
                .setBody("");

        return request;
    }

    public static SipRequest parse(String rawRequest) {
        SipRequest request = new SipRequest();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(rawRequest.split("\r\n")));

        try {
            String[] firstLineSplit = lines.get(0).split(" ");
            String method = firstLineSplit[0];
            request.setMethod(method);

            String name = null;
            String address = null;
            if(firstLineSplit[1].contains("@")){
                name = firstLineSplit[1].split("@")[0].split(":")[1];
                address = firstLineSplit[1].split("@")[1];
            }else{
                if(firstLineSplit[1].contains(";")){
                    address = firstLineSplit[1].substring(firstLineSplit[1].indexOf(":")+1,firstLineSplit[1].indexOf(";"));
                }else{
                    address = firstLineSplit[1].substring(firstLineSplit[1].indexOf(":")+1);
                }

            }

            request.setUriName(name);
            request.setUriAddress(address);

            if(hasParameter(firstLineSplit[1],"transport")){
                request.setUriTransport(getParameterValue(firstLineSplit[1],"transport" ));
            }

            request.setSipVersion(firstLineSplit[2].split("/")[1]);

            if (!Arrays.asList(allowedMethods).contains(method)) {
                throw new RuntimeException(String.format("Sip method %s is not supported", method));
            }

            request.setBody("");

            lines.remove(0);
            for (String line : lines) {
                parseLine(line, request);
                if (line.equals("")) {
                    break;
                }
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        return request;
    }

    private static void parseLine(String line, SipRequest request) {
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
                if(hasParameter(addressInfo,"branch")){
                    viaBranch = getParameterValue(addressInfo,"branch");
                }

                request.addVia(
                        protocolInfoSplit[1],
                        protocolInfoSplit[2],
                        addressInfoSplit[0],
                        viaBranch
                );

                break;
            case "max-forwards":
                request.addMaxForwards(Integer.parseInt(headerValue));
                break;
            case "contact":
                formatLine = headerValue
                        .replaceAll("<", "")
                        .replaceAll(">", "");

                lineSplit = formatLine.split(";");
                addressInfoSplit = lineSplit[0].split("@");

                String contactTransport = null;
                if (hasParameter(formatLine,"transport")){
                    contactTransport = getParameterValue(formatLine,"transport");
                }

                int contactExpires = -1;
                if(hasParameter(formatLine,"expires")){
                    contactExpires = Integer.parseInt(getParameterValue(headerValue,"expires"));
                }

                request.addContact(
                        addressInfoSplit[0].split(":")[1],
                        addressInfoSplit[1],
                        contactTransport,
                        contactExpires
                );
                break;
            case "to":
                formatLine = headerValue
                        .replaceAll("<", "")
                        .replaceAll(">", "");

                lineSplit = formatLine.split(";");
                addressInfoSplit = lineSplit[0].split("@");

                String toTransport = null;
                if(hasParameter(formatLine,"transport")){
                    toTransport = getParameterValue(formatLine,"transport");
                }

                String toTag = null;
                if(hasParameter(formatLine,"tag")){
                    toTag = getParameterValue(formatLine,"tag");
                }

                request.addTo(
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
                if(hasParameter(formatLine,"transport")){
                    fromTransport = getParameterValue(formatLine,"transport");
                }

                String fromTag = null;
                if(hasParameter(formatLine,"tag")){
                    fromTag = getParameterValue(formatLine,"tag");
                }

                request.addFrom(
                        addressInfoSplit[0].split(":")[1],
                        addressInfoSplit[1],
                        fromTransport,
                        fromTag
                );
                break;
            case "call-id":
                request.addCallId(headerValue);
                break;
            case "cseq":
                lineSplit = headerValue.split(" ");

                request.addCSeq(Integer.parseInt(lineSplit[0]), lineSplit[1]);
                break;
            case "expires":
                request.addExpires(Integer.parseInt(headerValue));
                break;
            case "allow":
                request.addAllowed(headerValue.split(","));
                break;
            case "supported":
                request.addSupported(headerValue.split(","));
                break;
            case "user-agent":
                request.addUserAgent(headerValue);
                break;
            case "allow-events":
                request.addAllowEvents(headerValue.split(","));
                break;
            case "content-length":
                request.addContentLength(Integer.parseInt(headerValue));
                break;
            case "authorization":
                String lineFormat = headerValue.trim();
                int firstSpace = lineFormat.indexOf(" ");
                String authType = lineFormat.substring(0,firstSpace);
                String paramInfo = lineFormat.substring(firstSpace).replaceAll("\"","").trim();
                String[] paramSplit = paramInfo.split(",");

                request.addAuth(
                        authType,
                        paramSplit[0].split("=")[1],
                        paramSplit[1].split("=")[1],
                        paramSplit[2].split("=")[1],
                        paramSplit[3].split("=")[1],
                        paramSplit[4].split("=")[1]
                );
                break;
        }
    }

    @Override
    public String toString() {
        String request = "";
        String requestLine = "";
        if(this.uriName != null){
            requestLine = String.format(
                    "%s sip:%s@%s;transport=%s SIP/%s\r\n",
                    getMethod(),
                    getUriName(),
                    getUriAddress(),
                    getUriTransport(),
                    getSipVersion());
        }else{
            requestLine = String.format(
                    "%s sip:%s;transport=%s SIP/%s\r\n",
                    getMethod(),
                    getUriAddress(),
                    getUriTransport(),
                    getSipVersion());
        }



        final AtomicReference<String> headers = new AtomicReference<>("");
        this.headers.forEach((name, value) -> headers.set(headers + String.format("%s: %s\r\n", name, value)));

        request += requestLine;
        request += headers;
        request += "\r\n";
        request += getBody();

        return request;
    }
}

package com.example.demo.service.asap.protocol;

import com.example.demo.service.asap.ASAPException;
import com.example.demo.service.utils.ASAPSerialization;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class AssimilationPDU_Impl extends PDU_Impl implements ASAP_AssimilationPDU_1_0 {
    private final long dataLength;
    private final InputStream is;
    private String recipientPeer;
    public static final String OFFSET_DELIMITER = ",";
    private List<Integer> offsets = new ArrayList<>();

    private byte[] data = null;
    private boolean dataNoLongerOnStream = false;

    // PDU: CMD | FLAGS | PEER | RECIPIENT | FORMAT | CHANNEL | ERA | OFFSETS | LENGTH | DATA

    public AssimilationPDU_Impl(int flagsInt, boolean encrypted, InputStream is) throws IOException, ASAPException {
        super(ASAP_1_0.ASSIMILATE_CMD, encrypted);

        evaluateFlags(flagsInt);

        if(this.senderSet()) { this.readSender(is); }
        if(this.recipientSet()) { this.readRecipientPeer(is); }
        this.readFormat(is);
        if(this.channelSet()) { this.readChannel(is); }
        if(this.eraSet()) { this.readEra(is); }
        if(this.offsetsSet()) { this.readOffsets(is); }

        this.dataLength = ASAPSerialization.readLongParameter(is);

        this.is = is;

        if(this.signed()) {
            // read data from stream, verification needs to reach signature
            this.getData();
        }
    }

    private void readOffsets(InputStream is) throws IOException, ASAPException {
        this.offsets = string2list(ASAPSerialization.readCharSequenceParameter(is));
    }

    private void readRecipientPeer(InputStream is) throws IOException, ASAPException {
        this.recipientPeer = ASAPSerialization.readCharSequenceParameter(is);
    }

    static void sendPDUWithoutCmd(CharSequence peer, CharSequence recipient, CharSequence format, CharSequence channel,
                                  int era, long length, List<Long> offsets, InputStream is, OutputStream os,
                                  boolean signed)
            throws IOException, ASAPException {

        // first: check protocol errors
        checkValidEra(era);
        checkValidFormat(format);
        checkValidStream(os);

        // create parameter bytes
        int flags = 0;
        flags = setFlag(peer, flags, SENDER_BIT_POSITION);
        flags = setFlag(recipient, flags, RECIPIENT_BIT_POSITION);
        flags = setFlag(channel, flags, CHANNEL_BIT_POSITION);
        flags = setFlag(era, flags, ERA_BIT_POSITION);
        flags = setFlag(offsets, flags, OFFSETS_BIT_POSITION);
        flags = setFlag(signed, flags, SIGNED_TO_BIT_POSITION);

        sendFlags(flags, os);

        ASAPSerialization.writeCharSequenceParameter(peer, os); // opt
        ASAPSerialization.writeCharSequenceParameter(recipient, os); // opt
        ASAPSerialization.writeCharSequenceParameter(format, os); // mand
        ASAPSerialization.writeCharSequenceParameter(channel, os); // opt
        ASAPSerialization.writeNonNegativeIntegerParameter(era, os); // opt
        ASAPSerialization.writeCharSequenceParameter(list2string(offsets), os); // opt

        ASAPSerialization.writeNonNegativeLongParameter(length, os); // mand

        // stream data
        while(length-- > 0) {
            os.write(is.read());
        }
    }

    static String list2string(List<Long> list) {
        if(list == null || list.size() == 0) return null;

        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for(Long i : list) {
            if(!first) {
                sb.append(OFFSET_DELIMITER);
            }
            else first = false;

            sb.append(i);
        }

        return  sb.toString();
    }

    private List<Integer> string2list(String s) throws ASAPException {
        List<Integer> l = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(s, OFFSET_DELIMITER);

        try {
            while (st.hasMoreTokens()) {
                l.add(Integer.parseInt(st.nextToken()));
            }
        }
        catch(RuntimeException re) {
            throw new ASAPException("malformed offset parameter in received data: " + s);
        }

        return l;

    }

    @Override
    public String getRecipientPeer() { return this.recipientPeer;  }

    @Override
    public long getLength() { return this.dataLength; }

    @Override
    public List<Integer> getMessageOffsets() {
        return this.offsets;
    }

    @Override
    public byte[] getData() throws IOException {
        if(data == null) {
            if(this.dataNoLongerOnStream)
                throw new IOException(this.getLogStart() + "data are already read from stream, probably with streamData");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.streamData(baos);
            this.data = baos.toByteArray();
        }

        return this.data;
    }

    private String getLogStart() {
        return this.getClass().getSimpleName() + ": ";
    }

    public InputStream getInputStream() throws IOException {
        if(this.dataNoLongerOnStream) {
            if(this.data == null) {
                throw new IOException(this.getLogStart()
                        + "data are no longer in stream, probably due to a previous call of streamData or getInputStream");
            } else {
                return new ByteArrayInputStream(this.data);
            }
        }

        this.dataNoLongerOnStream = true; // educated guess - they will be gone very soon.
        return this.is;
    }

    @Override
    public void streamData(OutputStream os) throws IOException {
        InputStream useIS;

        if(this.data == null) {
            if(this.dataNoLongerOnStream) {
                throw new IOException(this.getLogStart()
                        + "data are already read from stream, probably previous call of streamData");
            } else {
                // data are still on stream
                useIS = this.is;
                // but not any longer
                this.dataNoLongerOnStream = true;
            }
        } else {
            // already read
            useIS = new ByteArrayInputStream(this.data);
        }

        for (int i = 0; i < this.dataLength; i++) {
            os.write(useIS.read());
        }
    }
}

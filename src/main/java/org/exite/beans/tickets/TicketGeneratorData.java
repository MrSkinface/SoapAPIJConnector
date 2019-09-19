package org.exite.beans.tickets;

/**
 * Created by levitskym on 18.09.19
 */
public class TicketGeneratorData {

    private final byte[] baseTicketBody;
    private final String baseTicketSign;
    private final Signer signer;

    protected TicketGeneratorData(Init<?> init) {
        this.baseTicketBody = init.baseTicketBody;
        this.baseTicketSign = init.baseTicketSign;
        this.signer = init.signer;
    }

    public byte[] getBaseTicketBody() {
        return baseTicketBody;
    }

    public String getBaseTicketSign() {
        return baseTicketSign;
    }

    public Signer getSigner() {
        return signer;
    }

    protected static abstract class Init<T extends Init<T>> {
        private byte[] baseTicketBody;
        private String baseTicketSign;
        private Signer signer;

        protected abstract T self();

        public T setBaseTicketBody(byte[] baseTicketBody) {
            this.baseTicketBody = baseTicketBody;
            return self();
        }

        public T setBaseTicketSign(String baseTicketSign) {
            this.baseTicketSign = baseTicketSign;
            return self();
        }

        public T setSigner(Signer signer) {
            this.signer = signer;
            return self();
        }

        public TicketGeneratorData build() {
            return new TicketGeneratorData(this);
        }
    }

    public static class Builder extends Init<Builder> {
        @Override
        protected Builder self() {
            return this;
        }
    }
}

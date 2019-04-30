package ru.saidgadjiev.bibliographya.domain;

public class Verification {

    private String verificationKey;

    private String code;

    private long exipredAt;

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExipredAt() {
        return exipredAt;
    }

    public void setExipredAt(long exipredAt) {
        this.exipredAt = exipredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Verification that = (Verification) o;

        if (verificationKey != null ? !verificationKey.equals(that.verificationKey) : that.verificationKey != null)
            return false;
        return code != null ? code.equals(that.code) : that.code == null;
    }

    @Override
    public int hashCode() {
        int result = verificationKey != null ? verificationKey.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}

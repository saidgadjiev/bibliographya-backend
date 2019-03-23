package ru.saidgadjiev.bibliographya.html.truncate;

/**
 * Created by said on 23/03/2019.
 */
public class Options {

    private boolean keepImageTag = false;

    private boolean truncateLastWord = true;

    private int slop = 10;

    private String ellipsis = "...";

    public boolean isKeepImageTag() {
        return keepImageTag;
    }

    public Options keepImageTag(boolean keepImageTag) {
        this.keepImageTag = keepImageTag;

        return this;
    }

    public boolean isTruncateLastWord() {
        return truncateLastWord;
    }

    public Options truncateLastWord(boolean truncateLastWord) {
        this.truncateLastWord = truncateLastWord;

        return this;
    }

    public int getSlop() {
        return slop;
    }

    public Options slop(int slop) {
        this.slop = slop;

        return this;
    }

    public String getEllipsis() {
        return ellipsis;
    }

    public Options ellipsis(String ellipsis) {
        this.ellipsis = ellipsis;

        return this;
    }
}

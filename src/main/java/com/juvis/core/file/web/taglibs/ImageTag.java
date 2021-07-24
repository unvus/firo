package com.juvis.core.file.web.taglibs;

import javax.servlet.jsp.tagext.TagSupport;

public class ImageTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    private static final boolean ESCAPE_XML_DEFAULT = true;
    private boolean escapeXml;

    public ImageTag() {
        release();
    }

    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    @Override
    public int doStartTag() {

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() {

        return EVAL_PAGE;
    }

    @Override
    public void release() {
        escapeXml = ESCAPE_XML_DEFAULT;
    }
}

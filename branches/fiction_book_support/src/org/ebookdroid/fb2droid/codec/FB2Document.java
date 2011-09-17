package org.ebookdroid.fb2droid.codec;

import org.ebookdroid.EBookDroidApp;
import org.ebookdroid.core.OutlineLink;
import org.ebookdroid.core.PageLink;
import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.core.codec.CodecPage;
import org.ebookdroid.core.codec.CodecPageInfo;
import org.ebookdroid.core.log.LogContext;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

public class FB2Document implements CodecDocument {

    private static final LogContext LCTX = LogContext.ROOT.lctx("FB2");
    public static final int MAIN_TITLE_SIZE = 48;
    public static final int SECTION_TITLE_SIZE = 36;
    public static final int TEXT_SIZE = 24;
    public static final Paint NORMALTEXTPAINT = new TextPaint();
    public static final Paint MAINTITLETEXTPAINT = new TextPaint();
    public static final Paint SECTIONTITLETEXTPAINT = new TextPaint();
    private final ArrayList<FB2Page> pages = new ArrayList<FB2Page>();
    public static final Typeface NORMAL_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
    "fonts/OldStandard-Regular.ttf"); 
    public static final Typeface ITALIC_TF = Typeface.createFromAsset(EBookDroidApp.getAppContext().getAssets(),
    "fonts/OldStandard-Italic.ttf"); 

    public enum JustificationMode {
        Center, Left, Right, Justify;
    }

    public FB2Document(String fileName) {
        NORMALTEXTPAINT.setTextSize(TEXT_SIZE);
        MAINTITLETEXTPAINT.setTextSize(MAIN_TITLE_SIZE);
        SECTIONTITLETEXTPAINT.setTextSize(SECTION_TITLE_SIZE);

        NORMALTEXTPAINT.setAntiAlias(true);
        NORMALTEXTPAINT.setTypeface(NORMAL_TF);
        
        String encoding = getEncoding(fileName);

        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            SAXParser parser = spf.newSAXParser();
            
            Reader isr = new InputStreamReader(new FileInputStream(fileName), encoding);
            InputSource is = new InputSource();
            is.setCharacterStream(isr);
            parser.parse(is, new FB2SAXHandler(this));            
            
        } catch (Exception e) {
            throw new RuntimeException("FB2 document can not be opened: " + e.getMessage());
        }
    }

    private String getEncoding(String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            byte[] buffer = new byte[100];
            fis.read(buffer);
            Pattern p = Pattern.compile("encoding=\"(.*?)\"");
            Matcher matcher = p.matcher(new String(buffer));
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException e) {
        }
        return "UTF-8";
    }

    void appendLine(FB2Line line) {
        FB2Page lastPage = FB2Page.getLastPage(pages);

        if (lastPage.getContentHeight() + 2 * FB2Page.MARGIN_Y + line.getHeight() > FB2Page.PAGE_HEIGHT) {
            lastPage = new FB2Page();
            pages.add(lastPage);
        }
        lastPage.appendLine(line);

    }

    @Override
    public List<OutlineLink> getOutline() {
        return null;
    }

    @Override
    public CodecPage getPage(int pageNuber) {
        if (0 <= pageNuber && pageNuber < pages.size()) {
            return pages.get(pageNuber);
        } else {
            return null;
        }
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public CodecPageInfo getPageInfo(int pageNuber) {
        CodecPageInfo codecPageInfo = new CodecPageInfo();
        codecPageInfo.setWidth(FB2Page.PAGE_WIDTH);
        codecPageInfo.setHeight(FB2Page.PAGE_HEIGHT);
        return codecPageInfo;
    }

    @Override
    public List<PageLink> getPageLinks(int pageNuber) {
        return null;
    }

    @Override
    public boolean isRecycled() {
        return false;
    }

    @Override
    public void recycle() {
    }

    public void commitPage() {
        FB2Page lastPage = FB2Page.getLastPage(pages);
        int h = FB2Page.PAGE_HEIGHT - lastPage.getContentHeight() - 2 * FB2Page.MARGIN_Y;
        if (h > 0) {
            FB2Line line = new FB2Line();
            line.append(new FB2LineWhiteSpace(0, h, false));
            appendLine(line);
        }
        
    }

}

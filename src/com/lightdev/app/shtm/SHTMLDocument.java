/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.lightdev.app.shtm;

import javax.swing.text.html.HTMLDocument;

import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import java.net.*;
import java.util.*;
import javax.swing.undo.*;
import javax.swing.event.*;

/**
 * Extends <code>HTMLDocument</code> by a custom reader which supports
 * the SPAN tag.
 *
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version stage 11, April 27, 2003
 */

public class SHTMLDocument extends HTMLDocument {

  private AttributeContext context;

  /**
   * Constructs an SHTMLDocument.
   */
  public SHTMLDocument() {
    this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
  }

  /**
   * Constructs an SHTMLDocument with the default content
   * storage implementation and the given style/attribute
   * storage mechanism.
   *
   * @param styles  the styles
   */
  public SHTMLDocument(StyleSheet styles) {
    this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
  }

  /**
   * Constructs an SHTMLDocument with the given content
   * storage implementation and the given style/attribute
   * storage mechanism.
   *
   * @param c  the container for the content
   * @param styles the styles
   */
  public SHTMLDocument(Content c, StyleSheet styles) {
    super(c, styles);
  }

  /**
   * apply a set of attributes to a given document element
   *
   * @param e  the element to apply attributes to
   * @param a  the set of attributes to apply
   */
  public void addAttributes(Element e, AttributeSet a) {
    if ((e != null) && (a != null)) {
      try {
        writeLock();
        //System.out.println("SHTMLDocument addAttributes e=" + e);
        //System.out.println("SHTMLDocument addAttributes a=" + a);
        int start = e.getStartOffset();
        DefaultDocumentEvent changes = new DefaultDocumentEvent(start,
            e.getEndOffset() - start, DocumentEvent.EventType.CHANGE);
        AttributeSet sCopy = a.copyAttributes();
        MutableAttributeSet attr = (MutableAttributeSet) e.getAttributes();
        changes.addEdit(new AttributeUndoableEdit(e, sCopy, false));
        attr.addAttributes(a);
        changes.end();
        fireChangedUpdate(changes);
        fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
      }
      finally {
        writeUnlock();
      }
    }
  }

  /**
   * Remove a consecutive group of child Elements
   *
   * @param e  the parent element to remove child elements from
   * @param index  the index of the first child element to remove
   * @param count  the number of child elements to remove
   */
  public void removeElements(Element e, int index, int count)
      throws BadLocationException
  {
    writeLock();
    int start = e.getElement(index).getStartOffset();
    int end = e.getElement(index + count - 1).getEndOffset();
    try {
      Element[] removed = new Element[count];
      Element[] added = new Element[0];
      for (int counter = 0; counter < count; counter++) {
        removed[counter] = e.getElement(counter + index);
      }
      DefaultDocumentEvent dde = new DefaultDocumentEvent(
          start, end - start, DocumentEvent.EventType.REMOVE);
      ((AbstractDocument.BranchElement)e).replace(index, removed.length,
          added);
      dde.addEdit(new ElementEdit(e, index, removed, added));
      UndoableEdit u = getContent().remove(start, end - start);
      if (u != null) {
        dde.addEdit(u);
      }
      postRemoveUpdate(dde);
      dde.end();
      fireRemoveUpdate(dde);
      if (u != null) {
        fireUndoableEditUpdate(new UndoableEditEvent(this, dde));
      }
    }
    finally {
      writeUnlock();
    }
  }

  /* ------------------ custom document title handling start -------------------- */

  /**
   * set the title of this SHTMLDocument
   *
   * @param title  the title this document shall have
   */
  public void setDocumentTitle(String title) {
    try {
      String titleHTML = "<title></title>";
      Element defaultRoot = getDefaultRootElement();
      Element head = Util.findElementDown(HTML.Tag.HEAD.toString(), defaultRoot);
      if(head != null) {
        Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED.toString(), head);
        if(pImpl != null) {
          Element tElem = Util.findElementDown(HTML.Tag.TITLE.toString(), pImpl);
          if(tElem == null) {
            insertBeforeEnd(pImpl, titleHTML);
          }
        }
      }
      else {
        Element body = Util.findElementDown(HTML.Tag.BODY.toString(), defaultRoot);
        insertBeforeStart(body, "<head>" + titleHTML + "</head>");
      }
      putProperty(Document.TitleProperty, title);
    }
    catch(Exception e) {
      Util.errMsg(null, "An exception occurred while trying to insert the title", e);
    }
  }

  /**
   * get the title of this SHTMLDocument
   *
   * @return  the title of this document or null if none was set so far
   */
  public String getDocumentTitle() {
    Object title = getProperty(Document.TitleProperty);
    if(title != null) {
      return title.toString();
    }
    else {
      return null;
    }
  }

  /* ------------------ custom document title handling end -------------------- */

  /* ------------------ custom style sheet reference handling start -------------------- */

  /**
   * insert a style sheet reference into the head of this SHTMLDocument
   */
  public void insertStyleRef() {
    try {
      String styleRef = "  <link rel=stylesheet type=\"text/css\" href=\"" +
                        DocumentPane.DEFAULT_STYLE_SHEET_NAME + "\">";
      Element defaultRoot = getDefaultRootElement();
      Element head = Util.findElementDown(HTML.Tag.HEAD.toString(), defaultRoot);
      if(head != null) {
        Element pImpl = Util.findElementDown(HTML.Tag.IMPLIED.toString(), head);
        if(pImpl != null) {
          Element link = Util.findElementDown(HTML.Tag.LINK.toString(), pImpl);
          if(link != null) {
            setOuterHTML(link, styleRef);
          }
          else {
            insertBeforeEnd(pImpl, styleRef);
          }
        }
      }
      else {
        Element body = Util.findElementDown(HTML.Tag.BODY.toString(), defaultRoot);
        insertBeforeStart(body, "<head>" + styleRef + "</head>");
      }
    }
    catch(Exception e) {
      Util.errMsg(null, "An exception occurred while trying to insert the style sheet reference link", e);
    }
  }

  /**
   * check whether or not this SHTMLDocument has an explicit style sheet reference
   *
   * @return true, if a style sheet reference was found, false if not
   */
  public boolean hasStyleRef() {
    return (getStyleRef() != null);
  }

  /**
   * get the style sheet reference of the document in this
   * <code>DocumentPane</code>.
   *
   * @return the reference to this document's style sheet or
   *    null if none is found
   */
  public String getStyleRef() {
    String linkName = null;
    Element link = Util.findElementDown(HTML.Tag.LINK.toString(),
                                        getDefaultRootElement());
    if(link != null) {
      Object href = link.getAttributes().getAttribute(HTML.Attribute.HREF);
      if(href != null) {
        linkName = href.toString();
      }
    }
    return linkName;
  }

  /* ------------------ custom style sheet reference handling end -------------------- */

  /* -------- custom reader implementation start ------ */

  /**
   * Fetches the reader for the parser to use to load the document
   * with HTML.  This is implemented to return an instance of
   * SHTMLDocument.SHTMLReader.
   */
  public HTMLEditorKit.ParserCallback getReader(int pos) {
    Object desc = getProperty(Document.StreamDescriptionProperty);
    if (desc instanceof URL) {
        setBase((URL)desc);
    }
    SHTMLReader reader = new SHTMLReader(pos);
    return reader;
  }

  /**
   * This reader extends HTMLDocument.HTMLReader by the capability
   * to handle SPAN tags
   */
  public class SHTMLReader extends HTMLDocument.HTMLReader {

    /** action needed to handle SPAN tags */
    SHTMLCharacterAction ca = new SHTMLCharacterAction();

    /** the attributes found in a STYLE attribute */
    AttributeSet styleAttributes;

    /** indicates whether we're inside a SPAN tag */
    boolean inSpan = false;

    /**
     * Constructor
     */
    public SHTMLReader(int offset) {
      super(offset, 0, 0, null);
    }

    /**
     * handle a start tag received by the parser
     *
     * if it is a SPAN tag, convert the contents of the STYLE
     * attribute to an AttributeSet and add it to the contents
     * of this tag.
     *
     * Otherwise let HTMLDocument.HTMLReader do the work.
     */
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
      if(t == HTML.Tag.SPAN) {
        if(a.isDefined(HTML.Attribute.STYLE)) {
          String decl = (String)a.getAttribute(HTML.Attribute.STYLE);
          a.removeAttribute(HTML.Attribute.STYLE);
          styleAttributes = getStyleSheet().getDeclaration(decl);
          a.addAttributes(styleAttributes);
        }
        else {
          styleAttributes = null;
        }
        TagAction action = (TagAction) ca;

        if (action != null) {
          /**
           * remember which part we're in for handleSimpleTag
           */
          inSpan = true;

          action.start(t, a);
        }
      }
      else {
        super.handleStartTag(t, a, pos);
      }
    }

    /**
     * SPAN tags are directed to handleSimpleTag by the parser.
     * If a SPAN tag is detected in this method, it gets redirected
     * to handleStartTag and handleEndTag respectively.
     */
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
      if(t == HTML.Tag.SPAN) {
        if(inSpan) {
          handleEndTag(t, pos);
        }
        else {
          handleStartTag(t, a, pos);
        }
      }
      else {
        super.handleSimpleTag(t, a, pos);
      }
    }

    /**
     * If a SPAN tag is directed to this method, end its action,
     * otherwise, let HTMLDocument.HTMLReader do the work
     */
    public void handleEndTag(HTML.Tag t, int pos) {
      if(t == HTML.Tag.SPAN) {
        TagAction action = (TagAction) ca;
        if (action != null) {
          /**
           * remember which part we're in for handleSimpleTag
           */
          inSpan = false;

          action.end(t);
        }
      }
      else {
        super.handleEndTag(t, pos);
      }
    }

    /**
     * this action is used to read the style attribute from
     * a SPAN tag and to map from HTML to Java attributes.
     */
    class SHTMLCharacterAction extends HTMLDocument.HTMLReader.CharacterAction {
      public void start(HTML.Tag t, MutableAttributeSet attr) {
        pushCharacterStyle();
        if (attr.isDefined(IMPLIED)) {
          attr.removeAttribute(IMPLIED);
        }
        charAttr.addAttribute(t, attr.copyAttributes());
        if (styleAttributes != null) {
          charAttr.addAttributes(styleAttributes);
        }
        if(charAttr.isDefined(HTML.Tag.SPAN)) {
          charAttr.removeAttribute(HTML.Tag.SPAN);
        }
        //System.out.println("mapping attributes");
        charAttr = (MutableAttributeSet) new AttributeMapper(charAttr).
                   getMappedAttributes(AttributeMapper.toJava);
      }

      public void end(HTML.Tag t) {
        popCharacterStyle();
      }
    }
  }

  /* -------- custom reader implementation end -------- */

}

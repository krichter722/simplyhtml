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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.help.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.datatransfer.*;

import com.lightdev.app.shtm.actions.AppendTableColAction;
import com.lightdev.app.shtm.actions.AppendTableRowAction;
import com.lightdev.app.shtm.actions.BoldAction;
import com.lightdev.app.shtm.actions.DeleteTableColAction;
import com.lightdev.app.shtm.actions.DeleteTableRowAction;
import com.lightdev.app.shtm.actions.DocumentTitleAction;
import com.lightdev.app.shtm.actions.EditAnchorsAction;
import com.lightdev.app.shtm.actions.EditLinkAction;
import com.lightdev.app.shtm.actions.EditNamedStyleAction;
import com.lightdev.app.shtm.actions.FindReplaceAction;
import com.lightdev.app.shtm.actions.FontAction;
import com.lightdev.app.shtm.actions.FontFamilyAction;
import com.lightdev.app.shtm.actions.FontSizeAction;
import com.lightdev.app.shtm.actions.FormatImageAction;
import com.lightdev.app.shtm.actions.FormatListAction;
import com.lightdev.app.shtm.actions.FormatParaAction;
import com.lightdev.app.shtm.actions.FormatTableAction;
import com.lightdev.app.shtm.actions.GCAction;
import com.lightdev.app.shtm.actions.InsertImageAction;
import com.lightdev.app.shtm.actions.InsertTableAction;
import com.lightdev.app.shtm.actions.InsertTableColAction;
import com.lightdev.app.shtm.actions.InsertTableRowAction;
import com.lightdev.app.shtm.actions.ItalicAction;
import com.lightdev.app.shtm.actions.NextTableCellAction;
import com.lightdev.app.shtm.actions.PrevTableCellAction;
import com.lightdev.app.shtm.actions.RedoAction;
import com.lightdev.app.shtm.actions.SHTMLEditCopyAction;
import com.lightdev.app.shtm.actions.SHTMLEditCutAction;
import com.lightdev.app.shtm.actions.SHTMLEditPasteAction;
import com.lightdev.app.shtm.actions.SHTMLEditPrefsAction;
import com.lightdev.app.shtm.actions.SHTMLEditSelectAllAction;
import com.lightdev.app.shtm.actions.SHTMLFileCloseAction;
import com.lightdev.app.shtm.actions.SHTMLFileCloseAllAction;
import com.lightdev.app.shtm.actions.SHTMLFileExitAction;
import com.lightdev.app.shtm.actions.SHTMLFileNewAction;
import com.lightdev.app.shtm.actions.SHTMLFileOpenAction;
import com.lightdev.app.shtm.actions.SHTMLFileSaveAction;
import com.lightdev.app.shtm.actions.SHTMLFileSaveAllAction;
import com.lightdev.app.shtm.actions.SHTMLFileSaveAsAction;
import com.lightdev.app.shtm.actions.SHTMLFileTestAction;
import com.lightdev.app.shtm.actions.SHTMLHelpAppInfoAction;
import com.lightdev.app.shtm.actions.SetDefaultStyleRefAction;
import com.lightdev.app.shtm.actions.SetStyleAction;
import com.lightdev.app.shtm.actions.SetTagAction;
import com.lightdev.app.shtm.actions.ShowElementTreeAction;
import com.lightdev.app.shtm.actions.ToggleAction;
import com.lightdev.app.shtm.actions.ToggleListAction;
import com.lightdev.app.shtm.actions.UnderlineAction;
import com.lightdev.app.shtm.actions.UndoAction;
import com.lightdev.app.shtm.plugin.SHTMLPlugin;
import com.lightdev.app.shtm.plugin.PluginManager;
import com.lightdev.app.shtm.plugin.ManagePluginsAction;
import java.util.prefs.*;
import de.calcom.cclib.text.*;

/**
 * Main component of application SimplyHTML.
 *
 * <p>This class constructs the main panel and all of its GUI elements
 * such as menus, etc.</p>
 *
 * <p>It defines a set of inner classes creating actions which can be
 * connected to menus, buttons or instantiated individually.</p>
 *
 * @author Ulrich Hilger
 * @author Dimitri Polivaev
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version stage 12, August 06, 2006
 */

public class SHTMLPanel extends JPanel implements CaretListener, ChangeListener {

  //private int renderMode = SHTMLEditorKit.RENDER_MODE_JAVA;

  /* some public constants */
  public static final String APP_HELP_NAME = "help";
  public static final String APP_TEMP_DIR = "temp";
  public static final String IMAGE_DIR = "images";
  public static final String JAVA_HELP_EXT = ".hs";
  public static final String ACTION_SELECTED_KEY = "selected";
  public static final String ACTION_SELECTED = "true";
  public static final String ACTION_UNSELECTED = "false";
  public static final String FILE_LAST_OPEN = "lastOpenFileName";
  public static final String FILE_LAST_SAVE = "lastSaveFileName";

  /** single instance of a dynamic resource for use by all */
  public DynamicResource dynRes =
      new DynamicResource();

  /** SimplyHTML's main resource bundle (plug-ins use their own) */
  public static ResourceBundle resources = null;

  /** the plug-in manager of SimplyHTML */
  public static PluginManager pluginManager; // = new PluginManager(mainFrame);

  public static void setResources(ResourceBundle resources){
      SHTMLPanel.resources = resources;
  }
  private SHTMLMenuBar menuBar;

  /** number of currently active tab */
  private int activeTabNo;

    /** currently active DocumentPane */
  private DocumentPane dp;

  /** currently active SHTMLEditorPane */
  private SHTMLEditorPane editor;

  /** currently active SHTMLDocument */
  private SHTMLDocument doc;

  /** tool bar for formatting commands */
  private JToolBar formatToolBar;

  /** tool bar for formatting commands */
  private JToolBar paraToolBar;

  /** the tabbed pane for adding documents to show to */
  private JTabbedPane jtpDocs;

  /** our help broker */
  private static HelpBroker hb;

  /** plugin menu ID */
  public final String pluginMenuId = "plugin";

  /** help menu ID */
  public final String helpMenuId = "help";

  /** id in ResourceBundle for a relative path to an empty menu icon */
  private String emptyIcon = "emptyIcon";

  /** watch for repeated key events */
  private RepeatKeyWatcher rkw = new RepeatKeyWatcher(40);

  /** counter for newly created documents */
  int newDocCounter = 0;

  /** reference to applicatin temp directory */
  private static File appTempDir;

  /** tool bar selector for styles */
  private StyleSelector styleSelector;

  /** tool bar selector for certain tags */
  private TagSelector tagSelector;

  /** panel for plug-in display */
  SplitPanel sp;

  /** indicates, whether document activation shall be handled */
  boolean ignoreActivateDoc = false;

  /**
   * action names
   *
   * these have to correspond with the keys in the
   * resource bundle to allow for dynamic
   * menu creation and control
   */
  public static final String newAction = "new";
  public static final String openAction = "open";
  public static final String closeAction = "close";
  public static final String closeAllAction = "closeAll";
  public static  final String saveAction = "save";
  public static final String saveAsAction = "saveAs";
  public static  final String exitAction = "exit";
  public static  final String undoAction = "undo";
  public static  final String redoAction = "redo";
  public static  final String cutAction = "cut";
  public static  final String copyAction = "copy";
  public static  final String pasteAction = "paste";
  public static  final String selectAllAction = "selectAll";
  public static  final String fontAction = "font";
  public static  final String fontFamilyAction = "fontFamily";
  public static  final String fontSizeAction = "fontSize";
  public static  final String fontBoldAction = "fontBold";
  public static  final String fontItalicAction = "fontItalic";
  public static  final String fontUnderlineAction = "fontUnderline";
  public final String helpTopicsAction = "helpTopics";
  public static  final String aboutAction = "about";
  public static  final String gcAction = "gc";
  public static  final String elemTreeAction = "elemTree";
  public static  final String testAction = "test";
  public static  final String insertTableAction = "insertTable";
  public static  final String formatTableAction = "formatTable";
  public static  final String insertTableColAction = "insertTableCol";
  public static  final String insertTableRowAction = "insertTableRow";
  public static  final String appendTableRowAction = "appendTableRow";
  public static  final String appendTableColAction = "appendTableCol";
  public static  final String deleteTableRowAction = "deleteTableRow";
  public static  final String deleteTableColAction = "deleteTableCol";
  public static final String nextTableCellAction = "nextTableCell";
  public static final String prevTableCellAction = "prevTableCell";
  //public static final String nextCellAction = "nextCell";
  //public static final String prevCellAction = "prevCell";
  public static final String toggleBulletsAction = "toggleBullets";
  public static final String toggleNumbersAction = "toggleNumbers";
  public static  final String formatListAction = "formatList";
  public static final String editPrefsAction = "editPrefs";
  public static  final String insertImageAction = "insertImage";
  public static  final String formatImageAction = "formatImage";
  public static  final String setStyleAction = "setStyle";
  public static  final String formatParaAction = "formatPara";
  public static  final String editNamedStyleAction = "editNamedStyle";
  public static  final String paraAlignLeftAction = "paraAlignLeft";
  public static  final String paraAlignCenterAction = "paraAlignCenter";
  public static  final String paraAlignRightAction = "paraAlignRight";
  public static  final String insertLinkAction = "insertLink";
  public static  final String editLinkAction = "editLink";
  public static  final String setTagAction = "setTag";
  public static  final String editAnchorsAction = "editAnchors";
  public static final String saveAllAction = "saveAll";
  public static final String documentTitleAction = "documentTitle";
  public static final String setDefaultStyleRefAction = "setDefaultStyleRef";
  public static final String findReplaceAction = "findReplace";

  public static SHTMLPanel getOwnerSHTMLPanel(Component c){
      for(;;){
          if(c == null){
              return null;
          }
          if(c instanceof SHTMLPanel){
              return (SHTMLPanel)c;
          }
          c = c.getParent();
      }
  }
  /** construct a new main application frame */
  public SHTMLPanel() {
      super(new BorderLayout());
      if(resources == null)
      {
          try {
              resources = ResourceBundle.getBundle(
                      "com.lightdev.app.shtm.resources.SimplyHTML", Locale.getDefault());
          }
          catch(MissingResourceException mre) {
              Util.errMsg(null, "resources/SimplyHTML.properties not found", mre);
          }
      }
    SplashScreen splash = new SplashScreen();
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initActions();
    menuBar = dynRes.createMenubar(resources, "menubar");
    setJMenuBar(menuBar);
    customizeFrame();
    initAppTempDir();
    initPlugins();
    updateActions();
    initJavaHelp();
    splash.dispose();
    dynRes.getAction(newAction).actionPerformed(null);
    dp.getEditor().setCaretPosition(0);
  }

  private void setJMenuBar(JMenuBar bar) {
    add(bar, BorderLayout.NORTH);
}

  /* (non-Javadoc)
   * @see javax.swing.JComponent#processKeyBinding(javax.swing.KeyStroke, java.awt.event.KeyEvent, int, boolean)
   */
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
      if (super.processKeyBinding(ks, e, condition, pressed))
          return true;
      menuBar.handleKeyBinding(ks, e, condition, pressed);
      return true;
  }

/**
   * get the DynamicResource used in this instance of FrmMain
   *
   * @return the DynamicResource
   */
  public DynamicResource getDynRes() {
    return dynRes;
  }

  /**
   * get the resource bundle of this instance of FrmMain
   *
   * @return the bundle of resources
   */
  public ResourceBundle getResources() {
    return resources;
  }

  /**
   * get the temporary directory of SimplyHTML
   *
   * @return the temp dir
   */
  public static File getAppTempDir() {
    return appTempDir;
  }

  /**
   * get the file object for the document shown in the currently open DocumentPane
   *
   * @return the document file
   */
  public File getCurrentFile() {
    File file = null;
    URL url = dp.getSource();
    if(url != null) {
      file = new File(url.getFile());
    }
    return file;
  }

  /**
   * get the name of the file for the document shown in the currently open DocumentPane
   *
   * @return the document name
   */
  public String getCurrentDocName() {
    return dp.getDocumentName();
  }

    /**
     * Convenience method for obtaining the document text
     * @return returns the document text as string.
     */
    public String getDocumentText() {
        return dp.getDocumentText();
    }


    public Document getCurrentDocument() {
        return dp.getDocument();
    }

    /**
     * indicates whether or not the document needs to be saved.
     *
     * @return  true, if changes need to be saved
     */
    public boolean needsSaving() {
      return dp.needsSaving();
    }
    /**
     * Convenience method for clearing out the UndoManager
     */
    public void purgeUndos() {
        if(undo != null) {
            undo.discardAllEdits();
            dynRes.getAction(undoAction).putValue("enabled", Boolean.FALSE);
            dynRes.getAction(redoAction).putValue("enabled", Boolean.FALSE);
            updateFormatControls() ;
        }
    }
    /**
     * Convenience method for setting the document text
     */
    public void setCurrentDocumentContent(String docName, String sText) {
        jtpDocs.setTitleAt(getActiveTabNo(), docName);
        dp.setDocumentText(docName, sText);
        purgeUndos();
    }

    public void setContentPanePreferredSize(Dimension prefSize){
        dp.getEditor().setPreferredSize(prefSize);
        dp.getHtmlEditor().setPreferredSize(prefSize);
        dp.setPreferredSize(null);
    }
    /**
     * @return returns the currently used ExtendedHTMLDocument Object
     */
    public SHTMLDocument getExtendedHtmlDoc() {
        return doc;
    }

  /**
   * get the DocumentPane object that is currently active
   *
   * @return the active DocumentPane
   */
  public DocumentPane getCurrentDocumentPane() {
    return dp;
  }

  /**
   * add a DocumentPaneListener from the currently active DocumentPane (if any)
   */
  public void addDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
    if(dp != null) {
      //System.out.println("FrmMain.addDocumentPaneListener dp.source=" + dp.getSource());
      dp.addDocumentPaneListener(listener);
    }
    else {
      //System.out.println("FrmMain.addDocumentPaneListener dp is null, did not add");
    }
  }

  /**
   * remove a DocumentPaneListener from the currently active DocumentPane (if any)
   */
  public void removeDocumentPaneListener(DocumentPane.DocumentPaneListener listener) {
    if(dp != null) {
      dp.removeDocumentPaneListener(listener);
    }
  }

  /**
   * initialize SimplyHTML's temporary directory
   */
  private void initAppTempDir() {
    appTempDir = new File(System.getProperty("user.home") +
                          File.separator + FrmMain.APP_NAME +
                          File.separator + APP_TEMP_DIR + File.separator);
    if(!appTempDir.exists()) {
      appTempDir.mkdirs();
    }
  }

  /**
   * find plug-ins and load them accordingly,
   * i.e. display / dock components and add
   * plug-in menus.
   */
  public void initPlugins() {
    pluginManager = new PluginManager(this);
    JMenu pMenu = dynRes.getMenu(pluginMenuId);
    JMenu hMenu;
    if(pMenu != null) {
      Container contentPane = SHTMLPanel.this;
      pluginManager.loadPlugins();
      Enumeration plugins = pluginManager.plugins();
      SHTMLPlugin pi;
      JComponent pc;
      JMenuItem pluginMenu;
      JMenuItem helpMenu;
      while(plugins.hasMoreElements()) {
        pi = (SHTMLPlugin) plugins.nextElement();
        if(pi.isActive()) {
          refreshPluginDisplay(pi);
        }
      }
    }
    adjustDividers();
  }

  /**
   * adjust the divider sizes of SimplyHTML's SplitPanel
   * according to visibility
   */
  public void adjustDividers() {
    sp.adjustDividerSizes();
  }

  /**
   * watch for key events that are automatically repeated
   * due to the user holding down a key.
   *
   * <p>When a key is held down by the user, every keyPressed
   * event is followed by a keyTyped event and a keyReleased
   * event although the key is actually still down. I.e. it
   * can not be determined by a keyReleased event if a key
   * actually is released, which is why this implementation
   * is necessary.</p>
   */
  public class RepeatKeyWatcher implements KeyListener {

    /** timer for handling keyReleased events */
    private java.util.Timer releaseTimer = new java.util.Timer();

    /** the next scheduled task for a keyReleased event */
    private ReleaseTask nextTask;

    /** time of the last keyPressed event */
    private long lastWhen = 0;

    /** time of the current KeyEvent */
    private long when;

    /** delay to distinguish between single and repeated events */
    private long delay;

    /** indicates whether or not a KeyEvent currently occurs repeatedly */
    private boolean repeating = false;

    /**
     * construct a <code>RepeatKeyWatcher</code>
     *
     * @param delay  the delay in milliseconds until a
     * keyReleased event should be handled
     */
    public RepeatKeyWatcher(long delay) {
      super();
      this.delay = delay;
    }

    /**
     * handle a keyPressed event by cancelling the previous
     * release task (if any) and indicating repeated key press
     * as applicable.
     */
    public void keyPressed(KeyEvent e) {
      if(nextTask != null) {
        nextTask.cancel();
      }
      when = e.getWhen();
      if((when - lastWhen) <= delay) {
        repeating = true;
      }
      else {
        repeating = false;
      }
      lastWhen = when;
    }

    /**
     * handle a keyReleased event by scheduling a
     * <code>ReleaseTask</code>.
     */
    public void keyReleased(KeyEvent e) {
      nextTask = new ReleaseTask();
      releaseTimer.schedule(nextTask, delay);
    }

    public void keyTyped(KeyEvent e) { }

    /**
     * indicate whether or not a key is being held down
     *
     * @return true if a key is being held down, false if not
     */
    public boolean isRepeating() {
      return repeating;
    }

    /**
     * Task to be executed when a key is released
     */
    private class ReleaseTask extends TimerTask {
      public void run() {
        repeating = false;
        updateFormatControls();
      }
    }
  }

  public void clearDockPanels() {
    sp.removeAllOuterPanels();
  }

  /**
   * refresh the display for a given plug-in
   *
   * @param pi  the plug-in to refresh
   */
  public void refreshPluginDisplay(SHTMLPlugin pi) {
    JMenu pMenu = dynRes.getMenu(pluginMenuId);
    JMenu hMenu = dynRes.getMenu(helpMenuId);
    JMenuItem pluginMenu = pi.getPluginMenu();
    JMenuItem helpMenu = pi.getHelpMenu();
    JTabbedPane p = null;
    Preferences prefs;
    if(pi.isActive()) {
      JComponent pc = pi.getComponent();
      if(pc != null) {
        int panelNo = SplitPanel.WEST;
        double loc = 0.3;
        switch(pi.getDockLocation()) {
          case SHTMLPlugin.DOCK_LOCATION_LEFT:
            break;
          case SHTMLPlugin.DOCK_LOCATION_RIGHT:
            panelNo = SplitPanel.EAST;
            loc = 0.7;
            break;
          case SHTMLPlugin.DOCK_LOCATION_BOTTOM:
            panelNo = SplitPanel.SOUTH;
            loc = 0.7;
            break;
          case SHTMLPlugin.DOCK_LOCATION_TOP:
            panelNo = SplitPanel.NORTH;
            break;
        }
        p = (JTabbedPane) sp.getPanel(panelNo);
        p.setVisible(true);
        p.add(pi.getGUIName(), pc);
        if(((panelNo == SplitPanel.WEST) && sp.getDivLoc(panelNo) < this.getWidth() / 10) ||
           ((panelNo == SplitPanel.NORTH) && sp.getDivLoc(panelNo) < this.getHeight() / 10) ||
           ((panelNo == SplitPanel.EAST) && sp.getDivLoc(panelNo) > this.getWidth() - (this.getWidth() / 10)) ||
           ((panelNo == SplitPanel.SOUTH) && sp.getDivLoc(panelNo) > this.getHeight() - (this.getHeight() / 10)))
        {
          sp.setDivLoc(panelNo, loc);
        }
      }
      if(pluginMenu != null) {
        Icon menuIcon = pluginMenu.getIcon();
        if(menuIcon == null) {
          URL url = dynRes.getResource(resources, emptyIcon);
          if (url != null) {
            menuIcon = new ImageIcon(url);
            pluginMenu.setIcon(new ImageIcon(url));
          }
        }
        pMenu.add(pluginMenu);
      }
      if(helpMenu != null) {
        //System.out.println("FrmMain.refreshPluginDisplay insert helpMenu");
        if(helpMenu.getSubElements().length > 0) {
          Icon menuIcon = helpMenu.getIcon();
          if(menuIcon == null) {
            URL url = dynRes.getResource(resources, emptyIcon);
            if (url != null) {
              menuIcon = new ImageIcon(url);
              helpMenu.setIcon(new ImageIcon(url));
            }
          }
        }
        hMenu.insert(helpMenu, hMenu.getItemCount() - 2);
      }
      SwingUtilities.invokeLater(new PluginInfo(pi));
    }
    else {
      if(pluginMenu != null) {
        pMenu.remove(pluginMenu);
      }
      if(helpMenu != null) {
        hMenu.remove(helpMenu);
      }
    }
  }

  class PluginInfo implements Runnable {
    SHTMLPlugin pi;
    public PluginInfo(SHTMLPlugin pi) {
      this.pi = pi;
    }
    public void run() {
      pi.showInitialInfo();
    }
  }

  /**
   * get a <code>HelpBroker</code> for our application,
   * store it for later use and connect it to the help menu.
   */
  private void initJavaHelp() {
      try {
          JMenuItem mi = dynRes.getMenuItem(helpTopicsAction);
          if(mi == null){
              return;
          }
          CSH.setHelpIDString(mi, "item15");
          URL url = this.getClass().getResource(APP_HELP_NAME +
                  Util.URL_SEPARATOR + APP_HELP_NAME + JAVA_HELP_EXT);
          HelpSet hs = new HelpSet(null, url);
          hb = hs.createHelpBroker();
          mi.addActionListener(new CSH.DisplayHelpFromSource(getHelpBroker()));
          mi.setIcon(dynRes.getIconForCommand(resources, helpTopicsAction));
          mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
          mi.setEnabled(true);
      }
      catch (Exception e) {
          Util.errMsg(this,
                  dynRes.getResourceString(resources, "helpNotFoundError"),
                  e);
      }
  }

  /**
   * get the <code>HelpBroker</code> of our application
   *
   * @return the <code>HelpBroker</code> to be used for help display
   */
  public static HelpBroker getHelpBroker() {
    return hb;
  }

    /**
   * instantiate Actions and put them into the commands
   * Hashtable for later use along with their action commands.
   *
   * This is hard coded as Actions need to be instantiated
   * hard coded anyway, so we do the storage in <code>commands</code>
   * right away.
   */
  private void initActions() {
    dynRes.addAction(findReplaceAction, new FindReplaceAction(this));
    dynRes.addAction(setDefaultStyleRefAction, new SetDefaultStyleRefAction(this));
    dynRes.addAction(documentTitleAction, new DocumentTitleAction(this));
    dynRes.addAction(saveAllAction, new SHTMLFileSaveAllAction(this));
    dynRes.addAction(editAnchorsAction, new EditAnchorsAction(this));
    dynRes.addAction(setTagAction, new SetTagAction(this));
    dynRes.addAction(editLinkAction, new EditLinkAction(this));
    dynRes.addAction(prevTableCellAction, new PrevTableCellAction(this));
    dynRes.addAction(nextTableCellAction, new NextTableCellAction(this));
    dynRes.addAction(editNamedStyleAction, new EditNamedStyleAction(this));
    dynRes.addAction(formatParaAction, new FormatParaAction(this));
    dynRes.addAction(setStyleAction, new SetStyleAction(this));
    dynRes.addAction(formatImageAction, new FormatImageAction(this));
    dynRes.addAction(insertImageAction, new InsertImageAction(this));
    dynRes.addAction(editPrefsAction, new SHTMLEditPrefsAction(this));
    dynRes.addAction(toggleBulletsAction, new ToggleListAction(this, toggleBulletsAction, HTML.Tag.UL));
    dynRes.addAction(toggleNumbersAction, new ToggleListAction(this, toggleNumbersAction, HTML.Tag.OL));
    dynRes.addAction(formatListAction, new FormatListAction(this));
    dynRes.addAction(ManagePluginsAction.managePluginsAction,
                     new ManagePluginsAction());
    dynRes.addAction(newAction, new SHTMLFileNewAction(this));
    dynRes.addAction(openAction, new SHTMLFileOpenAction(this));
    dynRes.addAction(closeAction, new SHTMLFileCloseAction(this));
    dynRes.addAction(closeAllAction, new SHTMLFileCloseAllAction(this));
    dynRes.addAction(saveAction, new SHTMLFileSaveAction(this));
    dynRes.addAction(saveAsAction, new SHTMLFileSaveAsAction(this));
    dynRes.addAction(exitAction, new SHTMLFileExitAction(this));
    dynRes.addAction(elemTreeAction, new ShowElementTreeAction(this));
    dynRes.addAction(gcAction, new GCAction(this));
    dynRes.addAction(testAction, new SHTMLFileTestAction(this));
    dynRes.addAction(undoAction, new UndoAction(this));
    dynRes.addAction(redoAction, new RedoAction(this));
    dynRes.addAction(cutAction, new SHTMLEditCutAction(this));
    dynRes.addAction(copyAction, new SHTMLEditCopyAction(this));
    dynRes.addAction(pasteAction, new SHTMLEditPasteAction(this));
    dynRes.addAction(selectAllAction, new SHTMLEditSelectAllAction(this));
    dynRes.addAction(aboutAction, new SHTMLHelpAppInfoAction(this));
    dynRes.addAction(fontAction, new FontAction(this));
    dynRes.addAction(fontFamilyAction, new FontFamilyAction(this));
    dynRes.addAction(fontSizeAction, new FontSizeAction(this));
    dynRes.addAction(insertTableAction, new InsertTableAction(this));
    dynRes.addAction(insertTableRowAction, new InsertTableRowAction(this));
    dynRes.addAction(insertTableColAction, new InsertTableColAction(this));
    dynRes.addAction(appendTableColAction, new AppendTableColAction(this));
    dynRes.addAction(appendTableRowAction, new AppendTableRowAction(this));
    dynRes.addAction(deleteTableRowAction, new DeleteTableRowAction(this));
    dynRes.addAction(deleteTableColAction, new DeleteTableColAction(this));
    dynRes.addAction(formatTableAction, new FormatTableAction(this));
    dynRes.addAction(fontBoldAction, new BoldAction(this));
    dynRes.addAction(fontItalicAction, new ItalicAction(this));
    dynRes.addAction(fontUnderlineAction, new UnderlineAction(this));
    dynRes.addAction(paraAlignLeftAction, new ToggleAction(this, paraAlignLeftAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_LEFT));
    dynRes.addAction(paraAlignCenterAction, new ToggleAction(this, paraAlignCenterAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_CENTER));
    dynRes.addAction(paraAlignRightAction, new ToggleAction(this, paraAlignRightAction,
              CSS.Attribute.TEXT_ALIGN, Util.CSS_ATTRIBUTE_ALIGN_RIGHT));
  }

  /**
   * update all actions
   */
  public void updateActions() {
    Action action;
    Enumeration actions = dynRes.getActions();
    while(actions.hasMoreElements()) {
      action = (Action) actions.nextElement();
      if(action instanceof SHTMLAction) {
        ((SHTMLAction) action).update();
      }
    }
  }

  /** customize the frame to our needs */
  private void customizeFrame() {
    Container contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());

    sp = new SplitPanel();
    for(int i = 0; i < 4; i++) {
      JTabbedPane p = new JTabbedPane();
      p.setVisible(false);
      sp.addComponent(p, i);
    }

    jtpDocs = new JTabbedPane();
    jtpDocs.addChangeListener(this);
    sp.addComponent(jtpDocs, SplitPanel.CENTER);

    JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
    toolBarPanel.add(createToolBar("toolBar"));
    formatToolBar = createToolBar("formatToolBar");
    paraToolBar = createToolBar("paraToolBar");
    toolBarPanel.add(formatToolBar);
    toolBarPanel.add(paraToolBar);
    contentPane.add(toolBarPanel, BorderLayout.NORTH);
    //contentPane.add(workPanel, BorderLayout.CENTER);
    contentPane.add(sp, BorderLayout.CENTER);
    //contentPane.add(workPanel);
    add(contentPane, BorderLayout.CENTER);
  }

  /**
   * catch requests to close the application's main frame to
   * ensure proper clean up before the application is
   * actually terminated.
   */
  public boolean close() {
      dynRes.getAction(exitAction).actionPerformed(
                new ActionEvent(this, 0, exitAction));
      return jtpDocs.getTabCount() == 0;
  }

  /**
   * Create a tool bar.  This reads the definition of a tool bar
   * from the associated resource file.
   *
   * @param nm  the name of the tool bar definition in the resource file
   *
   * @return the created tool bar
   */
  public JToolBar createToolBar(String nm) {
    ToggleBorderListener tbl = new ToggleBorderListener();
    ButtonGroup bg = new ButtonGroup();
    Action action;
    AbstractButton newButton;
    Dimension buttonSize = new Dimension(24, 24);
    Dimension comboBoxSize = new Dimension(300, 24);
    Dimension separatorSize = new Dimension(3, 24);
    JSeparator separator;
    String[] itemKeys = Util.tokenize(
        dynRes.getResourceString(resources, nm), " ");
    JToolBar toolBar = new JToolBar();
    toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE );
    for (int i = 0; i < itemKeys.length; i++) {
      /** special handling for separators */
      if (itemKeys[i].equals(dynRes.menuSeparatorKey)) {
        separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMaximumSize(separatorSize);
        toolBar.add(separator);
      }
      /**
       * special handling for list elements in the
       * tool bar
       */
      else if(itemKeys[i].equalsIgnoreCase(fontFamilyAction)) {
	FontFamilyPicker fontFamily = new FontFamilyPicker();
        fontFamily.setPreferredSize(new Dimension(180, 23));
        fontFamily.setAction(dynRes.getAction(fontFamilyAction));
        fontFamily.setMaximumSize(comboBoxSize);
	toolBar.add(fontFamily);
      }
      else if(itemKeys[i].equalsIgnoreCase(fontSizeAction)) {
	FontSizePicker fontSize = new FontSizePicker();
        fontSize.setPreferredSize(new Dimension(50, 23));
        fontSize.setAction(dynRes.getAction(fontSizeAction));
        fontSize.setMaximumSize(comboBoxSize);
	toolBar.add(fontSize);
      }
      else if(itemKeys[i].equalsIgnoreCase(setStyleAction)) {
        styleSelector = new StyleSelector(HTML.Attribute.CLASS);
        styleSelector.setPreferredSize(new Dimension(110, 23));
        styleSelector.setAction(dynRes.getAction(setStyleAction));
        styleSelector.setMaximumSize(comboBoxSize);
        jtpDocs.addChangeListener(styleSelector);
        toolBar.add(styleSelector);
      }
      else if(itemKeys[i].equalsIgnoreCase(setTagAction)) {
        tagSelector = new TagSelector();
        tagSelector.setAction(dynRes.getAction(setTagAction));
        /*
        styleSelector = new StyleSelector(HTML.Attribute.CLASS);
        styleSelector.setPreferredSize(new Dimension(110, 23));
        styleSelector.setAction(dynRes.getAction(setStyleAction));
        styleSelector.setMaximumSize(comboBoxSize);
        jtpDocs.addChangeListener(styleSelector);
        */
        toolBar.add(tagSelector);
      }
      else if(itemKeys[i].equalsIgnoreCase(helpTopicsAction)) {
        newButton = new JButton();
        try {
          CSH.setHelpIDString(newButton, "item15");
          newButton.addActionListener(
              new CSH.DisplayHelpFromSource(getHelpBroker()));
          newButton.setIcon(dynRes.getIconForCommand(resources, itemKeys[i]));
          newButton.setToolTipText(dynRes.getResourceString(
              resources, itemKeys[i] + dynRes.toolTipSuffix));
          toolBar.add(newButton);
        }
        catch(Exception e) {}
      }
      else {
        action = dynRes.getAction(itemKeys[i]);
        /**
         * special handling for JToggleButtons in the tool bar
         */
        if(action instanceof AttributeComponent) {
          newButton =
              new JToggleButton("", (Icon) action.getValue(Action.SMALL_ICON));
          newButton.addMouseListener(tbl);
          newButton.setAction(action);
          newButton.setText("");
          //newButton.setActionCommand("");
          newButton.setBorderPainted(false);
          action.addPropertyChangeListener(new ToggleActionChangedListener((JToggleButton) newButton));
          Icon si = dynRes.getIconForName(resources, action.getValue(action.NAME) + DynamicResource.selectedIconSuffix);
          if(si != null) {
            newButton.setSelectedIcon(si);
          }
          newButton.setMargin(new Insets(0, 0, 0, 0));
          newButton.setIconTextGap(0);
          newButton.setContentAreaFilled(false);
          newButton.setHorizontalAlignment(SwingConstants.CENTER);
          newButton.setVerticalAlignment(SwingConstants.CENTER);
          toolBar.add(newButton);
          if(itemKeys[i].equalsIgnoreCase(paraAlignLeftAction) ||
             itemKeys[i].equalsIgnoreCase(paraAlignCenterAction) ||
             itemKeys[i].equalsIgnoreCase(paraAlignRightAction))
          {
            bg.add(newButton);
          }
        }
        /**
         * this is the usual way to add tool bar buttons finally
         */
        else {
          newButton = toolBar.add(action);
        }
        newButton.setMinimumSize(buttonSize);
        newButton.setPreferredSize(buttonSize);
        newButton.setMaximumSize(buttonSize);
        newButton.setFocusPainted(false);
        newButton.setRequestFocusEnabled(false);
      }
    }
    return toolBar;
  }

  /**
   * displays or removes an etched border around JToggleButtons
   * this listener is registered with.
   */
  private class ToggleBorderListener implements MouseListener {
    private EtchedBorder border = new EtchedBorder(EtchedBorder.LOWERED);
    private JToggleButton button;
    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) {
      Object src = e.getSource();
      if(src instanceof JToggleButton) {
        button = (JToggleButton) src;
        if(button.isEnabled()) {
          ((JToggleButton) src).setBorder(border);
        }
      }
    }
    public void mouseExited(MouseEvent e) {
      Object src = e.getSource();
      if(src instanceof JToggleButton) {
        ((JToggleButton) src).setBorder(null);
      }
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
  }

  /**
   * register FrmMain as an object which has interest
   * in events from a given document pane
   */
  public void registerDocument() {
    doc.addUndoableEditListener(undoHandler);
    getEditor().addCaretListener(this);
    getEditor().addKeyListener(rkw);
    ((SHTMLDocument) dp.getDocument()).getStyleSheet().addChangeListener(styleSelector);
  }

  /**
   * remove FrmMain as a registered object from a given
   * document pane and its components
   *
   * remove all plug-ins owned by this FrmMain from
   * SimplyHTML objects too
   */
  public void unregisterDocument() {
    getEditor().removeCaretListener(this);
    getEditor().removeKeyListener(rkw);
    if(doc != null) {
      doc.removeUndoableEditListener(undoHandler);
    }
    dp.removeAllListeners(); // for plug-in removal from any dp that is about to close
    doc.getStyleSheet().removeChangeListener(styleSelector);
    //System.out.println("FrmMain unregister document dp.name=" + dp.getDocumentName());
  }

  /**
   * save a document and catch possible errors
   *
   * this is shared by save and saveAs so we put it here to avoid redundancy
   *
   * @param dp  the document pane containing the document to save
   */
  public void doSave(DocumentPane dp) {
    try {
      dp.saveDocument();
    }
    /**
     * this exception should never happen as the menu allows to save a
     * document only if a name has been set. For new documents, whose
     * name is not set, only save as is enabled anyway.
     *
     * Just in case this is changed without remembering why it was designed
     * that way, we catch the exception here.
     */
    catch(DocNameMissingException e) {
      Util.errMsg(this, dynRes.getResourceString(resources, "docNameMissingError"), e);
    }
  }

  /**
   * get action properties from the associated resource bundle
   *
   * @param action the action to apply properties to
   * @param cmd the name of the action to get properties for
   */
  public static void getActionProperties(Action action, String cmd) {
    Icon icon = DynamicResource.getIconForCommand(resources, cmd);
    if (icon != null) {
      action.putValue(Action.SMALL_ICON, icon);
    }
    /*else {
      action.putValue(Action.SMALL_ICON, emptyIcon);
    }*/
    String toolTip = DynamicResource.getResourceString(resources, cmd + DynamicResource.toolTipSuffix);
    if(toolTip != null) {
      action.putValue(Action.SHORT_DESCRIPTION, toolTip);
    }
  }

  public int getActiveTabNo() {
    return activeTabNo;
  }

  /**
   * @param activeTabNo The activeTabNo to set.
   */
  public void setActiveTabNo(int activeTabNo) {
      this.activeTabNo = activeTabNo;
  }

  /**
   * change listener to be applied to our tabbed pane
   * so that always the currently active components
   * are known
   */
  public void stateChanged(ChangeEvent e) {
    activeTabNo = jtpDocs.getSelectedIndex();
    dp = (DocumentPane) jtpDocs.getComponentAt(activeTabNo);
    editor = dp.getEditor();
    //System.out.println("FrmMain stateChanged docName now " + dp.getDocumentName());
    doc = (SHTMLDocument) getEditor().getDocument();
    //fireDocumentChanged();
    if(!ignoreActivateDoc) {
      dp.fireActivated();
    }
  }

  /* ---------- undo/redo implementation ----------------------- */

  /** Listener for edits on a document. */
  private UndoableEditListener undoHandler = new UndoHandler();

  /** UndoManager that we add edits to. */
  private UndoManager undo = new UndoManager();

  /** inner class for handling undoable edit events */
  public class UndoHandler implements UndoableEditListener {
    /**
     * Messaged when the Document has created an edit, the edit is
     * added to <code>undo</code>, an instance of UndoManager.
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      // ignore all events happened when the html source code pane is open
      if(getCurrentDocumentPane().getSelectedTab() != DocumentPane.VIEW_TAB_LAYOUT){
          return;
      }
      getUndo().addEdit(e.getEdit());
    }
  }

  /**
   * caret listener implementation to track format changes
   */
  public void caretUpdate(CaretEvent e) {
    if(!rkw.isRepeating()) {
      updateFormatControls();
    }
  }

  /**
   * update any controls that relate to formats at the
   * current caret position
   */
  public void updateFormatControls() {
    updateAToolBar(formatToolBar);
    updateAToolBar(paraToolBar);
    Element e = doc.getParagraphElement(getEditor().getCaretPosition());
    if(tagSelector != null){
        SetTagAction sta = (SetTagAction) tagSelector.getAction();
        sta.setIgnoreActions(true);
        tagSelector.setSelectedTag(e.getName());
        sta.setIgnoreActions(false);
    }
  }

  private void updateAToolBar(JToolBar bar) {
    Component c;
    Action action;
    int count = bar.getComponentCount();
    AttributeSet a = getMaxAttributes(getEditor(), null);
    for(int i = 0; i < count; i++) {
      c = bar.getComponentAtIndex(i);
      if(c instanceof AttributeComponent) {
        if(c instanceof StyleSelector) {
          SetStyleAction ssa = (SetStyleAction) ((StyleSelector) c).getAction();
          ssa.setIgnoreActions(true);
          ((AttributeComponent) c).setValue(a);
          ssa.setIgnoreActions(false);
        }
        else {
          ((AttributeComponent) c).setValue(a);
        }
      }
      else if(c instanceof AbstractButton) {
        action = ((AbstractButton) c).getAction();
        if((action != null) && (action instanceof AttributeComponent)) {
          ((AttributeComponent) action).setValue(a);
        }
      }
    }
  }

    /**
   * a JComboBox for selecting a font family names
   * from those available in the system.
   */
  public class FontFamilyPicker extends JComboBox implements AttributeComponent {

    /** switch for the action listener */
    private boolean ignoreActions = false;

    FontFamilyPicker() {

      /**
       * add the font family names available in the system
       * to the combo box
       */
      super(GraphicsEnvironment.getLocalGraphicsEnvironment().
				      getAvailableFontFamilyNames());
    }

    public boolean ignore() {
      return ignoreActions;
    }

    /**
     * set the value of this <code>AttributeComponent</code>
     *
     * @param a  the set of attributes possibly having an
     *          attribute this component can display
     *
     * @return true, if the set of attributes had a matching attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      ignoreActions = true;
      final String newSelection  = Util.styleSheet().getFont(a).getFamily();
      setSelectedItem(newSelection);
      ignoreActions = false;
      return true;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      SimpleAttributeSet set = new SimpleAttributeSet();
      Util.styleSheet().addCSSAttribute(set, CSS.Attribute.FONT_FAMILY,
				(String) getSelectedItem());
      set.addAttribute(HTML.Attribute.FACE, (String) getSelectedItem());
      return set;
    }

    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }
  }

    /**
   * a JComboBox for selecting a font size
   */
  public class FontSizePicker extends JComboBox  implements AttributeComponent {
    private boolean ignoreActions = false;
    final private Object key;
    FontSizePicker() {
      /**
       * add font sizes to the combo box
       */
      super(new String[] {"8", "10", "12", "14", "18", "24"} );
      this.key = CSS.Attribute.FONT_SIZE;
    }

    public boolean ignore() {
      return ignoreActions;
    }

    /**
     * set the value of this combo box
     *
     * @param a  the set of attributes possibly having a
     *          font size attribute this pick list could display
     *
     * @return true, if the set of attributes had a font size attribute,
     *            false if not
     */
    public boolean setValue(AttributeSet a) {
      ignoreActions = true;
      final int size = Util.styleSheet().getFont(a).getSize();
      String newSelection = Integer.toString(size);
      setSelectedItem(newSelection);
      ignoreActions = false;
      return true;
    }

    /**
     * get the value of this <code>AttributeComponent</code>
     *
     * @return the value selected from this component
     */
    public AttributeSet getValue() {
      SimpleAttributeSet set = new SimpleAttributeSet();
      final String relativeSize = Integer.toString(getSelectedIndex() + 1);
      set.addAttribute(HTML.Attribute.SIZE, relativeSize);
    Util.styleSheet().addCSSAttributeFromHTML(set, CSS.Attribute.FONT_SIZE,
              relativeSize /*+ "pt"*/);
      return set;
    }
    public AttributeSet getValue(boolean includeUnchanged) {
      return getValue();
    }
  }

  /**
   * a listener for property change events on ToggleFontActions
   */
  private class ToggleActionChangedListener implements PropertyChangeListener {

    JToggleButton button;

    ToggleActionChangedListener(JToggleButton button) {
      super();
      this.button = button;
    }

    public void propertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if (e.getPropertyName().equals(SHTMLPanel.ACTION_SELECTED_KEY)) {
        //System.out.println("propertyName=" + propertyName + " newValue=" + e.getNewValue());
        if(e.getNewValue().toString().equals(SHTMLPanel.ACTION_SELECTED)) {
          button.setSelected(true);
        }
        else {
          button.setSelected(false);
        }
      }
    }
  }

  /**
   * Get all attributes that can be found in the element tree
   * starting at the highest parent down to the character element
   * at the current position in the document. Combine element
   * attributes with attributes from the style sheet.
   *
   * @param editor  the editor pane to combine attributes from
   *
   * @return the resulting set of combined attributes
   */
  public AttributeSet getMaxAttributes(SHTMLEditorPane editor,
                                       String elemName)
  {
    Element e = doc.getCharacterElement(editor.getSelectionStart());
    if(elemName != null && elemName.length() > 0) {
      e = Util.findElementUp(elemName, e);
    }
    StyleSheet s = doc.getStyleSheet();//((SHTMLEditorKit) editor.getEditorKit()).getStyleSheet();
    return getMaxAttributes(e, s);
  }

  public Frame getMainFrame() {
    return JOptionPane.getFrameForComponent(SHTMLPanel.this);
}

public static AttributeSet getMaxAttributes(Element e, StyleSheet s) {
    SimpleAttributeSet a = new SimpleAttributeSet();
    Element cElem = e;
    AttributeSet attrs;
    Vector elements = new Vector();
    Object classAttr;
    String styleName;
    String elemName;
    while(e != null) {
      elements.insertElementAt(e, 0);
      e = e.getParentElement();
    }
    for(int i = 0; i < elements.size(); i++) {
      e = (Element) elements.elementAt(i);
      classAttr = e.getAttributes().getAttribute(HTML.Attribute.CLASS);
      elemName = e.getName();
      styleName = elemName;
      if(classAttr != null) {
        styleName = elemName + "." + classAttr.toString();
        a.addAttribute(HTML.Attribute.CLASS, classAttr);
      }
      //System.out.println("getMaxAttributes name=" + styleName);
      attrs = s.getStyle(styleName);
      if(attrs != null) {
        a.addAttributes(Util.resolveAttributes(attrs));
      }
      else {
        attrs = s.getStyle(elemName);
        if(attrs != null) {
          a.addAttributes(Util.resolveAttributes(attrs));
        }
      }
      a.addAttributes(Util.resolveAttributes(e.getAttributes()));
    }
    if(cElem != null) {
      //System.out.println("getMaxAttributes cElem.name=" + cElem.getName());
      a.addAttributes(cElem.getAttributes());
    }
    //System.out.println(" ");
    //de.calcom.cclib.html.HTMLDiag hd = new de.calcom.cclib.html.HTMLDiag();
    //hd.listAttributes(a, 4);
    return new AttributeMapper(a).getMappedAttributes(AttributeMapper.toJava);
  }

/**
 * @param dp The dp to set.
 */
public void setDocumentPane(DocumentPane dp) {
    this.dp = dp;
}

/**
 * @return Returns the dp.
 */
public DocumentPane getDocumentPane() {
    return dp;
}

/**
 * @return Returns the editor.
 */
public SHTMLEditorPane getEditor() {
    return editor;
}

/**
 * @return Returns the doc.
 */
public SHTMLDocument getSHTMLDocument() {
    return doc;
}

/**
 * @return Returns the jtpDocs.
 */
public JTabbedPane getTabbedPaneForDocuments() {
    return jtpDocs;
}

/**
 * @param undo The undo to set.
 */
public void setUndo(UndoManager undo) {
    this.undo = undo;
}

/**
 * @return Returns the undo.
 */
public UndoManager getUndo() {
    return undo;
}

/**
 * @param tagSelector The tagSelector to set.
 */
public void setTagSelector(TagSelector tagSelector) {
    this.tagSelector = tagSelector;
}

/**
 * @return Returns the tagSelector.
 */
public TagSelector getTagSelector() {
    return tagSelector;
}

public void savePrefs() {
    sp.savePrefs();    
}

public void incNewDocCounter() {
    newDocCounter++;
}

public void createNewDocumentPane() {
    setDocumentPane(new DocumentPane(null, ++newDocCounter));
}

public void selectTabbedPane(int index) {
    ignoreActivateDoc = true;
    getTabbedPaneForDocuments().setSelectedIndex(index);
    ignoreActivateDoc = false;
}

  /* ---------- font manipulation code end ------------------ */

}
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

package com.lightdev.app.shtm.plugin;

import javax.swing.AbstractAction;
import java.awt.event.*;
import com.lightdev.app.shtm.SHTMLAction;
import com.lightdev.app.shtm.FrmMain;
import com.lightdev.app.shtm.Util;
import javax.swing.Action;
import com.lightdev.app.shtm.DialogShell;
import java.util.Enumeration;

/**
 * Action to invoke a PluginManagerDialog
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

public class ManagePluginsAction extends AbstractAction
    implements SHTMLAction
{
  public static final String managePluginsAction = "managePlugins";

  public ManagePluginsAction() {
    super(managePluginsAction);
    getProperties();
    /*putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
        KeyEvent.VK_N, KeyEvent.CTRL_MASK));*/
  }
  public void actionPerformed(ActionEvent e) {
    PluginManagerDialog pmd = new PluginManagerDialog(FrmMain.mainFrame,
        FrmMain.dynRes.getResourceString(FrmMain.resources,
        "pluginManagerDialogTitle"));
    Util.center(FrmMain.mainFrame, pmd);
    pmd.setModal(true);
    pmd.show();

    /** if the user made a selection, apply it to the document */
    if(pmd.getResult() == DialogShell.RESULT_OK) {
      ((FrmMain) FrmMain.mainFrame).clearDockPanels();
      Enumeration plugins = FrmMain.pluginManager.plugins();
      SHTMLPlugin pi;
      while(plugins.hasMoreElements()) {
        pi = (SHTMLPlugin) plugins.nextElement();
        ((FrmMain) FrmMain.mainFrame).refreshPluginDisplay(pi);
      }
      ((FrmMain) FrmMain.mainFrame).paintComponents(
          ((FrmMain) FrmMain.mainFrame).getGraphics());
    }
    ((FrmMain) FrmMain.mainFrame).adjustDividers();
    ((FrmMain) FrmMain.mainFrame).updateActions();
  }
  public void update() {
  }
  public void getProperties() {
    FrmMain.getActionProperties(this, (String) getValue(Action.NAME));
  }
}

package ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

/**
 * Composition of all {@link QueryResultTab}s
 */
public class QueryResultTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	private Map<Component, Tab> tabs;
	
	public QueryResultTabbedPane() {
		tabs = new HashMap<>();
		
		registerCloseActionKeyStroke();
		registerRefreshActionKeyStroke();
	}

	private void registerCloseActionKeyStroke() {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, Tab.COMMAND_CLOSE);
		Action closeCommand = new AbstractAction("Close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Optional<Tab> tab = getActiveTab();
				if (tab.isPresent()) {
					tab.get().close();
				}
			}
		};
		getActionMap().put(Tab.COMMAND_CLOSE, closeCommand);
	}
	
	private void registerRefreshActionKeyStroke() {
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, QueryResultTab.COMMAND_REFRESH);
		Action closeCommand = new AbstractAction("Refresh") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional<Tab> maybeTab = getActiveTab();
				if (maybeTab.isPresent()) {
					Tab tab = maybeTab.get();
					if (tab instanceof QueryResultTab) {
						((QueryResultTab<?>)tab).refresh();
					}
				}
			}
		};
		getActionMap().put(QueryResultTab.COMMAND_REFRESH, closeCommand);
	}

	public Optional<Tab> getActiveTab() {
		Component selectedComponent = this.getSelectedComponent();
		if (selectedComponent == null) {
			return Optional.empty();
		}
		return Optional.of(tabs.get(selectedComponent));
	}

	public void addTab(Component tabContentComponent, Tab tab) {
		tabs.put(tabContentComponent, tab);
	}
}

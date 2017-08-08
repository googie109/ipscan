/**
 * This file is a part of Angry IP Scanner source code,
 * see http://www.angryip.org/ for more information.
 * Licensed under GPLv2.
 */
package net.azib.ipscan.gui.actions;

import net.azib.ipscan.config.Labels;
import net.azib.ipscan.core.ScanningResult;
import net.azib.ipscan.core.ScanningResult.ResultType;
import net.azib.ipscan.core.ScanningResultList;
import net.azib.ipscan.gui.InputDialog;
import net.azib.ipscan.gui.ResultTable;
import net.azib.ipscan.gui.StatusBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.*;
import javax.inject.Inject;
import java.util.List;

/**
 * GotoActions
 *
 * @author Anton Keks
 */
public class GotoMenuActions {

	static class NextHost implements Listener {

		final ResultTable resultTable;
		final ResultType whatToSearchFor;
		
		NextHost(ResultTable resultTable, ResultType whatToSearchFor) {
			this.resultTable = resultTable;
			this.whatToSearchFor = whatToSearchFor;
		}
		
		protected int inc(int i) {
			return i+1;
		}
		
		protected int startIndex() {
			return resultTable.getSelectionIndex();
		}

		public final void handleEvent(Event event) {
			ScanningResultList results = resultTable.getScanningResults();
			
			int numElements = resultTable.getItemCount();
			int startIndex = startIndex();
			
			for (int i = inc(startIndex); i < numElements && i >= 0; i = inc(i)) {
				ScanningResult scanningResult = results.getResult(i);
				
				if (whatToSearchFor.matches(scanningResult.getType())) {
					resultTable.setSelection(i);
					resultTable.setFocus();
					return;
				}
				
			}
						
			// rewind
			if (startIndex >= 0 && startIndex < numElements) {
				resultTable.deselectAll();
				handleEvent(event);
			}
		}

	}
	
	static class PrevHost extends NextHost {

		public PrevHost(ResultTable resultTable, ResultType whatToSearchFor) {
			super(resultTable, whatToSearchFor);
		}

		protected int inc(int i) {
			return i-1;
		}

		protected int startIndex() {
			int curIndex = resultTable.getSelectionIndex();
			return curIndex >= 0 ? curIndex : resultTable.getItemCount();
		}
	}
	
	public static final class NextAliveHost extends NextHost {
		@Inject
		public NextAliveHost(ResultTable resultTable) {
			super(resultTable, ResultType.ALIVE);
		}
	}
	
	public static final class NextDeadHost extends NextHost {
		@Inject
		public NextDeadHost(ResultTable resultTable) {
			super(resultTable, ResultType.DEAD);
		}
	}
	
	public static final class NextHostWithInfo extends NextHost {
		@Inject
		public NextHostWithInfo(ResultTable resultTable) {
			super(resultTable, ResultType.WITH_PORTS);
		}
	}
	
	public static final class PrevAliveHost extends PrevHost {
		@Inject
		public PrevAliveHost(ResultTable resultTable) {
			super(resultTable, ResultType.ALIVE);
		}
	}
	
	public static final class PrevDeadHost extends PrevHost {
		@Inject
		public PrevDeadHost(ResultTable resultTable) {
			super(resultTable, ResultType.DEAD);
		}
	}
	
	public static final class PrevHostWithInfo extends PrevHost {
		@Inject
		public PrevHostWithInfo(ResultTable resultTable) {
			super(resultTable, ResultType.WITH_PORTS);
		}
	}
	
	public static final class Find implements Listener {

		private final ResultTable resultTable;
		private final StatusBar statusBar;
		private String lastText = "";

		@Inject
		public Find(StatusBar statusBar, ResultTable resultTable) {
			this.statusBar = statusBar;
			this.resultTable = resultTable;
		}
		
		public void handleEvent(Event event) {
			InputDialog dialog = new InputDialog(Labels.getLabel("title.find"), Labels.getLabel("text.find"));
			String text = dialog.open(lastText, Labels.getLabel("button.find.next"));
			//TODO: nbsafkjnsfksjfk
			if (text == null) {
				return;
			}
			lastText = text;
			
			try {
				statusBar.setStatusText(Labels.getLabel("state.searching"));
				findText(text, statusBar.getShell());
			}
			finally {
				statusBar.setStatusText(null);				
			}
		}

		private void clearHighlights(final ResultTable table) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < table.getItemCount(); i++) {
						TableItem item = resultTable.getItem(i);
						item.setBackground(null);
					}
				}
			});
		}

		private void findText(String text, Shell activeShell) {
			ScanningResultList results = resultTable.getScanningResults();

			// clear out previous result highlights
			clearHighlights(resultTable);
			
			int startIndex = resultTable.getSelectionIndex() + 1;
			final List<Integer> foundIndexes = results.findText(text, 0);

			if (foundIndexes.size() > 0) {
				// ensure start index is correct
				startIndex = getStartIndex(startIndex, foundIndexes);

				resultTable.setSelection(startIndex);
				resultTable.setFocus();
				resultTable.showSelection();

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						Device device = Display.getCurrent();
						// green color
						Color highlightColor = new Color(device, 50, 255, 50);
						for (Integer i : foundIndexes) {
							TableItem item = resultTable.getItem(i);
							if (!item.getText().equals("")) {
								item.setBackground(highlightColor);
							}
						}
					}
				});
				return;
			}

			if (startIndex > 0 && foundIndexes.size() > 0) {
				// if started not from the beginning, offer to restart				
				MessageBox messageBox = new MessageBox(activeShell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
				messageBox.setText(Labels.getLabel("title.find"));
				messageBox.setMessage(Labels.getLabel("text.find.notFound") + " " + Labels.getLabel("text.find.restart"));
				if (messageBox.open() == SWT.YES) {
					resultTable.deselectAll();
					findText(text, activeShell);
				}
				// reset back to the first table row if we didn't find anything
				resultTable.setSelection(0);
				resultTable.setFocus();
			}
			else {
				// searching is finished, nothing was found				
				MessageBox messageBox = new MessageBox(activeShell, SWT.OK | SWT.ICON_INFORMATION);
				messageBox.setText(Labels.getLabel("title.find"));
				messageBox.setMessage(Labels.getLabel("text.find.notFound"));
				messageBox.open();
			}
		}
	}

	private static int getStartIndex(int startIndex, List<Integer> foundIndexes) {
		// if found, then select and finish
		boolean foundValidIndex = false;

		for (int i = 0; i < foundIndexes.size() && !foundValidIndex; i++) {
			// actual index value
			int indexValue = foundIndexes.get(i);

			// check to see if index value is equal to our current selection index
			if (startIndex == indexValue) {
				foundValidIndex = true;
			}
		}

		if (!foundValidIndex && foundIndexes.size() > 0) {
			startIndex = foundIndexes.get(0);
		}
		return startIndex;
	}

}

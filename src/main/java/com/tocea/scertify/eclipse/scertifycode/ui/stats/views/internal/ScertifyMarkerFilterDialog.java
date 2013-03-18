// ============================================================================
//
// Copyright (C) 2002-2006 David Schneider, Lars K�dderitzsch, Fabrice Bellingard
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// ============================================================================

package com.tocea.scertify.eclipse.scertifycode.ui.stats.views.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import com.tocea.scertify.eclipse.scertifycode.ui.ScertifyUIPluginImages;
import com.tocea.scertify.eclipse.scertifycode.ui.stats.Messages;

/**
 * Dialog to edit the marker filter.
 * 
 * @author Lars K�dderitzsch
 */
public class ScertifyMarkerFilterDialog extends TitleAreaDialog
{
    
    //
    // attributes
    //
    
    /**
     * The controller for this dialog.
     * 
     * @author Lars K�dderitzsch
     */
    private class PageController implements SelectionListener
    {
        
        /**
         * updates the enablement state of the controls.
         */
        private void updateControlState() {
        
            ScertifyMarkerFilterDialog.this.mFilterComposite
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mRadioOnAnyResource
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mRadioAnyResourceInSameProject
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mRadioSelectedResource
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mRadioSelectedResourceAndChildren
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mRadioSelectedWorkingSet
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mLblSelectedWorkingSet
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mBtnWorkingSet.setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled
                    .getSelection() && ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mChkSeverityEnabled
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            
            ScertifyMarkerFilterDialog.this.mChkSeverityError
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection()
                            && ScertifyMarkerFilterDialog.this.mChkSeverityEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mChkSeverityWarning
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection()
                            && ScertifyMarkerFilterDialog.this.mChkSeverityEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mChkSeverityInfo
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection()
                            && ScertifyMarkerFilterDialog.this.mChkSeverityEnabled.getSelection());
            
            ScertifyMarkerFilterDialog.this.mGrpRegex.setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled
                    .getSelection());
            ScertifyMarkerFilterDialog.this.mChkSelectByRegex
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mLblRegexFilter
                    .setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled.getSelection());
            ScertifyMarkerFilterDialog.this.mBtnEditRegex.setEnabled(ScertifyMarkerFilterDialog.this.mChkFilterEnabled
                    .getSelection());
        }
        
        /**
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
        
        public void widgetDefaultSelected(SelectionEvent e) {
        
            // NOOP
        }
        
        /**
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        
        public void widgetSelected(SelectionEvent e) {
        
            if (e.widget == ScertifyMarkerFilterDialog.this.mChkFilterEnabled
                    || e.widget == ScertifyMarkerFilterDialog.this.mChkSeverityEnabled) {
                this.updateControlState();
            } else if (ScertifyMarkerFilterDialog.this.mBtnDefault == e.widget) {
                ScertifyMarkerFilterDialog.this.mFilter.resetState();
                ScertifyMarkerFilterDialog.this.updateUIFromFilter();
            } else if (ScertifyMarkerFilterDialog.this.mBtnWorkingSet == e.widget) {
                final IWorkingSetSelectionDialog dialog =
                        PlatformUI.getWorkbench().getWorkingSetManager()
                                .createWorkingSetSelectionDialog(ScertifyMarkerFilterDialog.this.getShell(), false);
                
                if (ScertifyMarkerFilterDialog.this.mSelectedWorkingSet != null) {
                    dialog.setSelection(new IWorkingSet[] { ScertifyMarkerFilterDialog.this.mSelectedWorkingSet });
                }
                if (dialog.open() == Window.OK) {
                    final IWorkingSet[] result = dialog.getSelection();
                    if (result != null && result.length > 0) {
                        ScertifyMarkerFilterDialog.this.mSelectedWorkingSet = result[0];
                    } else {
                        ScertifyMarkerFilterDialog.this.mSelectedWorkingSet = null;
                    }
                    ScertifyMarkerFilterDialog.this.initWorkingSetLabel();
                }
            } else if (ScertifyMarkerFilterDialog.this.mBtnEditRegex == e.widget) {
                final List regex = new ArrayList(ScertifyMarkerFilterDialog.this.mRegularExpressions);
                final RegexDialog dialog = new RegexDialog(ScertifyMarkerFilterDialog.this.getShell(), regex);
                if (Window.OK == dialog.open()) {
                    ScertifyMarkerFilterDialog.this.mRegularExpressions = regex;
                    ScertifyMarkerFilterDialog.this.initRegexLabel();
                }
            }
        }
    }
    
    /**
     * Dialog to edit regular expressions to filter by.
     * 
     * @author Lars K�dderitzsch
     */
    private class RegexDialog extends TitleAreaDialog
    {
        
        private ListViewer mListViewer;
        
        private Button     mAddButton;
        
        private Button     mRemoveButton;
        
        private Text       mRegexText;
        
        private final List mFileTypesList;
        
        /**
         * Creates a file matching pattern editor dialog.
         * 
         * @param parentShell
         *            the parent shell
         * @param pattern
         *            the pattern
         */
        public RegexDialog(Shell parentShell, List fileTypes) {
        
            super(parentShell);
            this.mFileTypesList = fileTypes;
        }
        
        /**
         * Over-rides method from Window to configure the shell (e.g. the
         * enclosing window).
         */
        
        protected void configureShell(Shell shell) {
        
            super.configureShell(shell);
            shell.setText(Messages.ScertifyMarkerFilterDialog_titleRegexEditor);
        }
        
        /**
         * Creates the content assistant.
         * 
         * @return the content assistant
         */
        private/* SubjectControlContentAssistant */IContentAssistant createContentAssistant() {
        
            // Deprecated version
            /*
             * final SubjectControlContentAssistant contentAssistant = new
             * SubjectControlContentAssistant();
             * contentAssistant.setRestoreCompletionProposalSize(ScertifyUIPlugin
             * .getDefault().getDialogSettings());
             * final IContentAssistProcessor processor = new
             * RegExContentAssistProcessor( true);
             * contentAssistant.setContentAssistProcessor(processor,
             * IDocument.DEFAULT_CONTENT_TYPE); contentAssistant
             * .setContextInformationPopupOrientation
             * (IContentAssistant.CONTEXT_INFO_ABOVE); contentAssistant
             * .setInformationControlCreator(new IInformationControlCreator() {
             */
            /*
             * @see org.eclipse.jface.text.IInformationControlCreator
             * #createInformationControl( org.eclipse.swt.widgets.Shell)
             */
            /*
             * public IInformationControl createInformationControl( Shell
             * parent) { return new DefaultInformationControl(parent); } });
             */
            
            // New version
            final IContentAssistant contentAssistant = new ContentAssistant();
            // TODO : may need more configuration
            
            return contentAssistant;
        }
        
        /**
         * @see Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        
        protected Control createDialogArea(Composite parent) {
        
            final Composite composite = (Composite) super.createDialogArea(parent);
            
            final Composite main = new Composite(composite, SWT.NONE);
            GridLayout layout = new GridLayout(2, false);
            main.setLayout(layout);
            GridData gd = new GridData(GridData.FILL_BOTH);
            main.setLayoutData(gd);
            
            final Composite controls = new Composite(main, SWT.NONE);
            layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            controls.setLayout(layout);
            controls.setLayoutData(new GridData(GridData.FILL_BOTH));
            
            final Composite buttons = new Composite(main, SWT.NONE);
            layout = new GridLayout(1, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            buttons.setLayout(layout);
            buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
            
            this.mRegexText = new Text(controls, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            this.mRegexText.setLayoutData(gd);
            
            this.mAddButton = new Button(buttons, SWT.PUSH);
            this.mAddButton.setText(Messages.ScertifyMarkerFilterDialog_btnAdd);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.verticalAlignment = SWT.TOP;
            this.mAddButton.setLayoutData(gd);
            this.mAddButton.addSelectionListener(new SelectionListener()
            {
                
                
                public void widgetDefaultSelected(SelectionEvent e) {
                
                    // NOOP
                }
                
                
                public void widgetSelected(SelectionEvent e) {
                
                    final String text = RegexDialog.this.mRegexText.getText();
                    if (text.trim().length() > 0) {
                        
                        try {
                            // check for the patterns validity
                            Pattern.compile(text);
                            
                            RegexDialog.this.mFileTypesList.add(text);
                            RegexDialog.this.mListViewer.refresh();
                            RegexDialog.this.mRegexText.setText(""); //$NON-NLS-1$
                            
                        } catch (final PatternSyntaxException ex) {
                            RegexDialog.this.setErrorMessage(NLS.bind(
                                    Messages.ScertifyMarkerFilterDialog_msgInvalidRegex, ex.getLocalizedMessage()));
                        }
                    }
                }
            });
            
            this.mListViewer = new ListViewer(controls, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
            this.mListViewer.setLabelProvider(new LabelProvider());
            this.mListViewer.setContentProvider(new ArrayContentProvider());
            this.mListViewer.setInput(this.mFileTypesList);
            gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 100;
            gd.widthHint = 150;
            gd.grabExcessHorizontalSpace = true;
            this.mListViewer.getControl().setLayoutData(gd);
            
            this.mRemoveButton = new Button(buttons, SWT.PUSH);
            this.mRemoveButton.setText(Messages.ScertifyMarkerFilterDialog_btnRemove);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.verticalAlignment = SWT.TOP;
            this.mRemoveButton.setLayoutData(gd);
            this.mRemoveButton.addSelectionListener(new SelectionListener()
            {
                
                
                public void widgetDefaultSelected(SelectionEvent e) {
                
                    // NOOP
                }
                
                
                public void widgetSelected(SelectionEvent e) {
                
                    final IStructuredSelection selection =
                            (IStructuredSelection) RegexDialog.this.mListViewer.getSelection();
                    RegexDialog.this.mFileTypesList.remove(selection.getFirstElement());
                    RegexDialog.this.mListViewer.refresh();
                }
            });
            
            // integrate content assist
            // ContentAssistHandler.createHandlerForText(this.mRegexText,
            // this.createContentAssistant());
            
            // TODO : need to add content assist handler
            
            this.setTitle(Messages.ScertifyMarkerFilterDialog_titleRegexEditor);
            this.setMessage(Messages.ScertifyMarkerFilterDialog_msgEditRegex);
            
            return main;
        }
        
        /**
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        
        protected void okPressed() {
        
            super.okPressed();
        }
    }
    
    private Button                     mChkFilterEnabled;
    
    private Button                     mRadioOnAnyResource;
    
    private Button                     mRadioAnyResourceInSameProject;
    
    private Button                     mRadioSelectedResource;
    
    private Button                     mRadioSelectedResourceAndChildren;
    
    private Button                     mRadioSelectedWorkingSet;
    
    private Label                      mLblSelectedWorkingSet;
    
    private Button                     mBtnWorkingSet;
    
    private Button                     mChkSeverityEnabled;
    
    private Button                     mChkSeverityError;
    
    private Button                     mChkSeverityWarning;
    
    private Button                     mChkSeverityInfo;
    
    private Composite                  mFilterComposite;
    
    private Group                      mGrpRegex;
    
    private Button                     mChkSelectByRegex;
    
    private Label                      mLblRegexFilter;
    
    private Button                     mBtnEditRegex;
    
    private Button                     mBtnDefault;
    
    /** The filter to be edited. */
    private final ScertifyMarkerFilter mFilter;
    
    /** the selected working set. */
    private IWorkingSet                mSelectedWorkingSet;
    
    //
    // constructors
    //
    
    /** The controller of this dialog. */
    private final PageController       mController = new PageController();
    
    //
    // methods
    //
    
    /** The regular expressions to filter by. */
    private List                       mRegularExpressions;
    
    /**
     * Creates the filter dialog.
     * 
     * @param shell
     *            the parent shell
     * @param filter
     *            the filter instance
     */
    public ScertifyMarkerFilterDialog(Shell shell, ScertifyMarkerFilter filter) {
    
        super(shell);
        this.mFilter = filter;
    }
    
    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    
    protected void configureShell(Shell shell) {
    
        super.configureShell(shell);
        shell.setText(Messages.ScertifyMarkerFilterDialog_btnShellTitle);
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    
    protected void createButtonsForButtonBar(Composite parent) {
    
        this.mBtnDefault =
                this.createButton(parent, IDialogConstants.BACK_ID,
                        Messages.ScertifyMarkerFilterDialog_btnRestoreDefault, false);
        this.mBtnDefault.addSelectionListener(this.mController);
        
        // create OK and Cancel buttons by default
        this.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        this.createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    
    protected Control createDialogArea(Composite parent) {
    
        final Composite composite = (Composite) super.createDialogArea(parent);
        
        final Composite dialog = new Composite(composite, SWT.NONE);
        dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
        dialog.setLayout(new GridLayout(1, false));
        
        this.mChkFilterEnabled = new Button(dialog, SWT.CHECK);
        this.mChkFilterEnabled.setText(Messages.ScertifyMarkerFilterDialog_btnEnabled);
        this.mChkFilterEnabled.addSelectionListener(this.mController);
        
        final Group onResourceGroup = new Group(dialog, SWT.NULL);
        onResourceGroup.setText(Messages.ScertifyMarkerFilterDialog_groupResourceSetting);
        onResourceGroup.setLayout(new GridLayout(3, false));
        onResourceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.mFilterComposite = onResourceGroup;
        
        this.mRadioOnAnyResource = new Button(onResourceGroup, SWT.RADIO);
        this.mRadioOnAnyResource.setText(Messages.ScertifyMarkerFilterDialog_btnOnAnyResource);
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        this.mRadioOnAnyResource.setLayoutData(gd);
        
        this.mRadioAnyResourceInSameProject = new Button(onResourceGroup, SWT.RADIO);
        this.mRadioAnyResourceInSameProject.setText(Messages.ScertifyMarkerFilterDialog_btnOnAnyResourceInSameProject);
        gd = new GridData();
        gd.horizontalSpan = 3;
        this.mRadioAnyResourceInSameProject.setLayoutData(gd);
        
        this.mRadioSelectedResource = new Button(onResourceGroup, SWT.RADIO);
        this.mRadioSelectedResource.setText(Messages.ScertifyMarkerFilterDialog_btnOnSelectedResource);
        gd = new GridData();
        gd.horizontalSpan = 3;
        this.mRadioSelectedResource.setLayoutData(gd);
        
        this.mRadioSelectedResourceAndChildren = new Button(onResourceGroup, SWT.RADIO);
        this.mRadioSelectedResourceAndChildren
                .setText(Messages.ScertifyMarkerFilterDialog_btnOnSelectedResourceAndChilds);
        gd = new GridData();
        gd.horizontalSpan = 3;
        this.mRadioSelectedResourceAndChildren.setLayoutData(gd);
        
        this.mRadioSelectedWorkingSet = new Button(onResourceGroup, SWT.RADIO);
        this.mRadioSelectedWorkingSet.setText(Messages.ScertifyMarkerFilterDialog_btnOnWorkingSet);
        this.mRadioSelectedWorkingSet.setLayoutData(new GridData());
        
        this.mLblSelectedWorkingSet = new Label(onResourceGroup, SWT.NULL);
        this.mLblSelectedWorkingSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_BEGINNING));
        
        this.mBtnWorkingSet = new Button(onResourceGroup, SWT.PUSH);
        this.mBtnWorkingSet.setText(Messages.ScertifyMarkerFilterDialog_btnSelect);
        gd = new GridData();
        gd.horizontalSpan = 1;
        gd.verticalSpan = 2;
        this.mBtnWorkingSet.setLayoutData(gd);
        this.mBtnWorkingSet.addSelectionListener(this.mController);
        
        final Composite severityGroup = new Composite(onResourceGroup, SWT.NULL);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = 0;
        severityGroup.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        severityGroup.setLayoutData(gd);
        
        this.mChkSeverityEnabled = new Button(severityGroup, SWT.CHECK);
        this.mChkSeverityEnabled.setText(Messages.ScertifyMarkerFilterDialog_btnMarkerSeverity);
        this.mChkSeverityEnabled.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.mChkSeverityEnabled.addSelectionListener(this.mController);
        
        this.mChkSeverityError = new Button(severityGroup, SWT.CHECK);
        this.mChkSeverityError.setText(Messages.ScertifyMarkerFilterDialog_btnSeverityError);
        this.mChkSeverityError.setLayoutData(new GridData());
        
        this.mChkSeverityWarning = new Button(severityGroup, SWT.CHECK);
        this.mChkSeverityWarning.setText(Messages.ScertifyMarkerFilterDialog_btnSeverityWarning);
        this.mChkSeverityWarning.setLayoutData(new GridData());
        
        this.mChkSeverityInfo = new Button(severityGroup, SWT.CHECK);
        this.mChkSeverityInfo.setText(Messages.ScertifyMarkerFilterDialog_btnSeverityInfo);
        this.mChkSeverityInfo.setLayoutData(new GridData());
        
        this.mGrpRegex = new Group(dialog, SWT.NULL);
        this.mGrpRegex.setText(Messages.ScertifyMarkerFilterDialog_lblExcludeMarkers);
        layout = new GridLayout(3, false);
        this.mGrpRegex.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        this.mGrpRegex.setLayoutData(gd);
        
        this.mChkSelectByRegex = new Button(this.mGrpRegex, SWT.CHECK);
        this.mChkSelectByRegex.setText(Messages.ScertifyMarkerFilterDialog_lblRegex);
        this.mChkSelectByRegex.setLayoutData(new GridData());
        
        this.mLblRegexFilter = new Label(this.mGrpRegex, SWT.NULL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        this.mLblRegexFilter.setLayoutData(gd);
        
        this.mBtnEditRegex = new Button(this.mGrpRegex, SWT.PUSH);
        this.mBtnEditRegex.setText(Messages.ScertifyMarkerFilterDialog_btnEdit);
        this.mBtnEditRegex.setLayoutData(new GridData());
        this.mBtnEditRegex.addSelectionListener(this.mController);
        
        // init the controls
        this.updateUIFromFilter();
        
        this.setTitleImage(ScertifyUIPluginImages.getImage(ScertifyUIPluginImages.PLUGIN_LOGO));
        this.setTitle(Messages.ScertifyMarkerFilterDialog_title);
        this.setMessage(Messages.ScertifyMarkerFilterDialog_titleMessage);
        
        return composite;
    }
    
    /**
     * Returns the edited filter.
     * 
     * @return the edited filter
     */
    public ScertifyMarkerFilter getFilter() {
    
        return this.mFilter;
    }
    
    /**
     * Initializes the label for the regular expressions.
     */
    private void initRegexLabel() {
    
        final StringBuffer buf = new StringBuffer();
        
        final int size = this.mRegularExpressions != null ? this.mRegularExpressions.size() : 0;
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                buf.append(", "); //$NON-NLS-1$
            }
            buf.append(this.mRegularExpressions.get(i));
        }
        
        if (size == 0) {
            buf.append(Messages.ScertifyMarkerFilterDialog_msgNoRegexDefined);
        }
        
        this.mLblRegexFilter.setText(buf.toString());
    }
    
    /**
     * Initializes the label for the selected working set.
     */
    private void initWorkingSetLabel() {
    
        if (this.mSelectedWorkingSet == null) {
            this.mLblSelectedWorkingSet.setText(Messages.ScertifyMarkerFilterDialog_msgNoWorkingSetSelected);
        } else {
            this.mLblSelectedWorkingSet.setText(this.mSelectedWorkingSet.getName());
        }
    }
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    
    protected void okPressed() {
    
        this.updateFilterFromUI();
        super.okPressed();
    }
    
    /**
     * Updates the filter data from the ui controls.
     */
    private void updateFilterFromUI() {
    
        this.mFilter.setEnabled(this.mChkFilterEnabled.getSelection());
        
        if (this.mRadioSelectedResource.getSelection()) {
            this.mFilter.setOnResource(ScertifyMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        } else if (this.mRadioSelectedResourceAndChildren.getSelection()) {
            this.mFilter.setOnResource(ScertifyMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        } else if (this.mRadioAnyResourceInSameProject.getSelection()) {
            this.mFilter.setOnResource(ScertifyMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        } else if (this.mRadioSelectedWorkingSet.getSelection()) {
            this.mFilter.setOnResource(ScertifyMarkerFilter.ON_WORKING_SET);
        } else {
            this.mFilter.setOnResource(ScertifyMarkerFilter.ON_ANY_RESOURCE);
        }
        
        this.mFilter.setWorkingSet(this.mSelectedWorkingSet);
        
        this.mFilter.setSelectBySeverity(this.mChkSeverityEnabled.getSelection());
        int severity = 0;
        if (this.mChkSeverityError.getSelection()) {
            severity = severity | ScertifyMarkerFilter.SEVERITY_ERROR;
        }
        if (this.mChkSeverityWarning.getSelection()) {
            severity = severity | ScertifyMarkerFilter.SEVERITY_WARNING;
        }
        if (this.mChkSeverityInfo.getSelection()) {
            severity = severity | ScertifyMarkerFilter.SEVERITY_INFO;
        }
        this.mFilter.setSeverity(severity);
        
        this.mFilter.setFilterByRegex(this.mChkSelectByRegex.getSelection());
        this.mFilter.setFilterRegex(this.mRegularExpressions);
    }
    
    /**
     * Updates the ui controls from the filter data.
     */
    private void updateUIFromFilter() {
    
        this.mChkFilterEnabled.setSelection(this.mFilter.isEnabled());
        
        this.mRadioOnAnyResource.setSelection(this.mFilter.getOnResource() == ScertifyMarkerFilter.ON_ANY_RESOURCE);
        this.mRadioAnyResourceInSameProject
                .setSelection(this.mFilter.getOnResource() == ScertifyMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT);
        this.mRadioSelectedResource
                .setSelection(this.mFilter.getOnResource() == ScertifyMarkerFilter.ON_SELECTED_RESOURCE_ONLY);
        this.mRadioSelectedResourceAndChildren
                .setSelection(this.mFilter.getOnResource() == ScertifyMarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN);
        this.mRadioSelectedWorkingSet.setSelection(this.mFilter.getOnResource() == ScertifyMarkerFilter.ON_WORKING_SET);
        
        this.mSelectedWorkingSet = this.mFilter.getWorkingSet();
        this.initWorkingSetLabel();
        
        this.mChkSeverityEnabled.setSelection(this.mFilter.getSelectBySeverity());
        
        this.mChkSeverityError.setSelection((this.mFilter.getSeverity() & ScertifyMarkerFilter.SEVERITY_ERROR) > 0);
        this.mChkSeverityWarning.setSelection((this.mFilter.getSeverity() & ScertifyMarkerFilter.SEVERITY_WARNING) > 0);
        this.mChkSeverityInfo.setSelection((this.mFilter.getSeverity() & ScertifyMarkerFilter.SEVERITY_INFO) > 0);
        
        this.mChkSelectByRegex.setSelection(this.mFilter.isFilterByRegex());
        this.mRegularExpressions = this.mFilter.getFilterRegex();
        this.initRegexLabel();
        
        this.mController.updateControlState();
    }
}

package com.kms.katalon.composer.keyword.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.keyword.constants.ComposerKeywordMessageConstants;
import com.kms.katalon.composer.keyword.constants.StringConstants;

public class NewKeywordDialog extends CommonAbstractKeywordDialog {
    
    public static final int SAMPLE_WEB_KEYWORD = 1;
    
    public static final int SAMPLE_MOBILE_KEYWORD = 2;
    
    public static final int SAMPLE_API_KEYWORD = 4;

    private IPackageFragment parentPackage;

    private IPackageFragmentRoot rootPackage;

    private Text txtPackage;

    private Button btnBrowse;
    
    private Button btnGenerateSampleWebKeyword;
    
    private Button btnGenerateSampleMobileKeyword;
    
    private Button btnGenerateSampleAPIKeyword;
    
    private int sampleKeywordType = 0;

    private ValidatorManager validatorManager;

    private Validator packageValidator;

    private Validator nameValidator;

    public NewKeywordDialog(Shell parentShell, IPackageFragmentRoot rootPackage, IPackageFragment parentPackage) {
        super(parentShell, null);
        setDialogTitle(StringConstants.DIA_TITLE_KEYWORD);
        setDialogMsg(StringConstants.DIA_MSG_CREATE_KEYWORD);
        this.rootPackage = rootPackage;
        this.parentPackage = parentPackage;
        setHelpAvailable(true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control area = super.createDialogArea(parent);
        setInput();
        addControlModifyListeners();
        return area;
    }

    private void setInput() {
        txtName.forceFocus();
    }

    @Override
    public Control createDialogBodyArea(Composite parent) {
        if (container == null) {
            container = new Composite(parent, SWT.NONE);
        }
        
        createPackageNameControl(container, 3);
        super.setLblName(StringConstants.MSG_CLASS_NAME_TITLE);
        return super.createDialogBodyArea(parent);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        composite.setFont(parent.getFont());

        // create help control if needed
        if (isHelpAvailable()) {
            Control helpControl = createHelpControl(composite);
            ((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        }
        doCreateButtonBar(composite);

        return composite;
    }
    
    protected Control doCreateButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
                | GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }
    
    @Override
    protected Control createHelpControl(Composite parent) {
        Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
        if (helpImage != null) {
            return createHelpImageButton(parent, helpImage);
        }
        return createHelpLink(parent);
    }

    private Link createHelpLink(Composite parent) {
        Link link = new Link(parent, SWT.WRAP | SWT.NO_FOCUS);
        ((GridLayout) parent.getLayout()).numColumns++;
        link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        link.setText("<a>"+ "https://vnexpress.net/"+"</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        link.setToolTipText(IDialogConstants.HELP_LABEL);
        return link;
    }
    
    private ToolBar createHelpImageButton(Composite parent, Image image) {
        ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
        ((GridLayout) parent.getLayout()).numColumns++;
//        toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
        final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
        toolBar.setCursor(cursor);
        toolBar.addDisposeListener(e -> cursor.dispose());
        ToolItem fHelpButton = new ToolItem(toolBar, SWT.CHECK);
        fHelpButton.setImage(image);
        fHelpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openBrowserToLink("https://docs.katalon.com/x/8gAM");
            }
        });
        return toolBar;
    }

    @Override
    protected Control createEntityCustomControl(Composite parent, int column, int span) {
        return createSampleKeywordControl(parent, column);
    }
    
    private void addControlModifyListeners() {
        prepareValidators();

        btnBrowse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (choosePackage()) {
                    txtPackage.setText(getParentPackage().getElementName());
                }
            }
        });
        txtPackage.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent event) {
                String newName = getTextFromVerifyEvent(event);
                validatePackageName(newName);
                updateStatus();
            }
        });

        txtName.removeListener(SWT.Modify, txtName.getListeners(SWT.Modify)[0]);
        txtName.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent event) {
                String newName = getTextFromVerifyEvent(event);
                if (!newName.equals(txtName)) {
                    validateKeywordName(newName, parentPackage);
                    updateStatus();
                    setName(newName);
                }
            }
        });
        
        BiFunction<Integer, Button, SelectionAdapter> selectionAdapterCreator = (sampleType,
                sampleButton) -> new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (sampleButton.getSelection()) {
                            sampleKeywordType |= sampleType;
                        } else {
                            sampleKeywordType &= ~sampleType;
                        }
                    }
                };
        btnGenerateSampleWebKeyword.addSelectionListener(
                selectionAdapterCreator.apply(SAMPLE_WEB_KEYWORD, btnGenerateSampleWebKeyword));
        btnGenerateSampleMobileKeyword.addSelectionListener(
                selectionAdapterCreator.apply(SAMPLE_MOBILE_KEYWORD, btnGenerateSampleMobileKeyword));
        btnGenerateSampleAPIKeyword.addSelectionListener(
                selectionAdapterCreator.apply(SAMPLE_API_KEYWORD, btnGenerateSampleAPIKeyword));
    }
    
    

    private void prepareValidators() {
        validatorManager = new ValidatorManager();

        nameValidator = new Validator(getDialogMsg());
        nameValidator.setOK(false);
        validatorManager.addValidator(nameValidator);

        packageValidator = new Validator();
        packageValidator.setOK(true);
        validatorManager.addValidator(packageValidator);
    }

    @Override
    public void updateStatus() {
        setMessage(validatorManager.getMessage(), validatorManager.getType());
        super.getButton(OK).setEnabled(validatorManager.isOK());
    }

    private String getTextFromVerifyEvent(VerifyEvent event) {
        Text txt = (Text) event.widget;
        StringBuilder builder = new StringBuilder(txt.getText());
        return builder.replace(event.start, event.end, event.text).toString();
    }

    private Control createSampleKeywordControl(Composite parent, int column) {
        Composite sampleKeywordComposite = new Composite(parent, SWT.NONE);
        sampleKeywordComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, column, 1));
        sampleKeywordComposite.setLayout(new GridLayout(1, false));
        
        btnGenerateSampleWebKeyword = new Button(sampleKeywordComposite, SWT.CHECK);
        btnGenerateSampleWebKeyword.setText(ComposerKeywordMessageConstants.DIA_LBL_GENERATE_SAMPLE_FOR_WEB);
        
        btnGenerateSampleMobileKeyword = new Button(sampleKeywordComposite, SWT.CHECK);
        btnGenerateSampleMobileKeyword.setText(ComposerKeywordMessageConstants.DIA_LBL_GENERATE_SAMPLE_FOR_MOBILE);
        
        btnGenerateSampleAPIKeyword = new Button(sampleKeywordComposite, SWT.CHECK);
        btnGenerateSampleAPIKeyword.setText(ComposerKeywordMessageConstants.DIA_LBL_GENERATE_SAMPLE_FOR_API);
        
        return parent;
    }
    
    private Control createPackageNameControl(Composite parent, int column) {
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        parent.setLayout(new GridLayout(column, false));
        Label labelName = new Label(parent, SWT.NONE);
        labelName.setText(StringConstants.PACKAGE);

        txtPackage = new Text(parent, SWT.BORDER);
        txtPackage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        txtPackage.setText(getParentPackage().getElementName());
        txtPackage.selectAll();

        btnBrowse = new Button(parent, SWT.PUSH);
        btnBrowse.setText(StringConstants.BROWSE);
        return parent;
    }

    @Override
    public void validateEntityName(String entityName) throws Exception {
        validatePackageName(txtPackage.getText());
        validateKeywordName(entityName, parentPackage);
    }

    private void validatePackageName(String packageName) {
        try {
            packageValidator.reset();
            if (packageName.isEmpty()) {
                packageValidator.setMessage(StringConstants.DIA_WARN_DEFAULT_PACKAGE, IMessageProvider.WARNING);
                packageValidator.setOK(false);
                return;
            }

            IPackageFragment pkg = rootPackage.getPackageFragment(packageName);
            validatePackageName(packageName, pkg);
            parentPackage = pkg;
            packageValidator.setOK(true);
        } catch (Exception e) {
            packageValidator.setMessage(e.getMessage(), IMessageProvider.ERROR);
            packageValidator.setOK(false);
        }
    }

    @Override
    protected void validateKeywordName(String name, IPackageFragment parentPackage) {
        try {
            nameValidator.reset();
            if (name.isEmpty()) {
                nameValidator.setOK(false);
                return;
            }
            super.validateKeywordName(name, parentPackage);
            nameValidator.setOK(true);
        } catch (Exception e) {
            nameValidator.setMessage(e.getMessage(), IMessageProvider.ERROR);
            nameValidator.setOK(false);
        }
    }

    protected boolean choosePackage() {
        IJavaElement[] packages = null;
        try {
            if (rootPackage != null && rootPackage.exists()) {
                packages = rootPackage.getChildren();
            }
        } catch (JavaModelException e) {
            LoggerSingleton.logError(e);
        }
        if (packages == null) {
            packages = new IJavaElement[0];
        }

        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_DEFAULT));
        dialog.setIgnoreCase(false);
        dialog.setTitle(StringConstants.DIA_TITLE_PACKAGE_SELECTION);
        dialog.setMessage(StringConstants.DIA_MSG_CHOOSE_A_PACKAGE);
        dialog.setEmptyListMessage(StringConstants.DIA_MSG_NO_PACKAGE);
        dialog.setElements(packages);
        dialog.setHelpAvailable(false);

        if (parentPackage != null) {
            dialog.setInitialSelections(new Object[] { parentPackage });
        }

        int dialogStatus = dialog.open();
        if (isOK(dialogStatus)) {
            parentPackage = (IPackageFragment) dialog.getFirstResult();
        }

        return isOK(dialogStatus);
    }

    private boolean isOK(int dialogStatus) {
        return dialogStatus == Window.OK;
    }

    public IPackageFragment getParentPackage() {
        return parentPackage;
    }
    
    public int getSampleKeywordType() {
        return sampleKeywordType;
    }
    
    private class Validator {
        private String message;

        private String defaultMessage;

        private int serverity;

        private boolean ok;

        public Validator() {
            this(StringUtils.EMPTY);
        }

        public Validator(String defaultMessage) {
            this.defaultMessage = defaultMessage;
            reset();
        }

        public void reset() {
            setMessage(defaultMessage);
            setOK(false);
            getServerity(IMessageProvider.INFORMATION);
        }

        public void setMessage(String message, int serverity) {
            setMessage(message);
            getServerity(serverity);
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getServerity() {
            return serverity;
        }

        public void getServerity(int serverity) {
            this.serverity = serverity;
        }

        public boolean isOK() {
            return ok;
        }

        public void setOK(boolean ok) {
            this.ok = ok;
        }
    }

    private class ValidatorManager {
        private List<Validator> validators;

        public ValidatorManager() {
            validators = new ArrayList<>();
        }

        private void addValidator(Validator newValidator) {
            validators.add(newValidator);
        }

        public String getMessage() {
            return getHighestServerityValidator().getMessage();
        }

        public int getType() {
            return getHighestServerityValidator().getServerity();
        }

        public boolean isOK() {
            if (validators.isEmpty()) {
                return true;
            }
            for (Validator validator : validators) {
                if (!validator.isOK()) {
                    return false;
                }
            }
            return true;
        }

        private Validator getHighestServerityValidator() {
            if (validators.isEmpty()) {
                return null;
            }
            Validator highest = validators.get(0);
            for (Validator validator : validators) {
                if (highest.getServerity() < validator.getServerity()) {
                    highest = validator;
                }
            }
            return highest;
        }
    }
    
    private void openBrowserToLink(String url) {
        Program.launch(url);
    }
}

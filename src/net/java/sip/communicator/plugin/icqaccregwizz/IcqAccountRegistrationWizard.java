/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 * 
 * Distributable under LGPL license. See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.icqaccregwizz;

import java.awt.*;
import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;

import org.osgi.framework.*;

/**
 * The <tt>IcqAccountRegistrationWizard</tt> is an implementation of the
 * <tt>AccountRegistrationWizard</tt> for the ICQ protocol. It should allow
 * the user to create and configure a new ICQ account.
 * 
 * @author Yana Stamcheva
 */
public class IcqAccountRegistrationWizard
    implements AccountRegistrationWizard
{
    private FirstWizardPage firstWizardPage;

    private IcqAccountRegistration registration = new IcqAccountRegistration();

    private WizardContainer wizardContainer;

    private ProtocolProviderService protocolProvider;

    private boolean isModification;

    /**
     * Creates an instance of <tt>IcqAccountRegistrationWizard</tt>.
     * 
     * @param wizardContainer the wizard container, where this wizard is added
     */
    public IcqAccountRegistrationWizard(WizardContainer wizardContainer)
    {
        this.wizardContainer = wizardContainer;

        this.wizardContainer.setFinishButtonText(Resources.getString("signin"));
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getIcon</code> method.
     * Returns the icon to be used for this wizard.
     */
    public byte[] getIcon()
    {
        return Resources.getImage(Resources.ICQ_LOGO);
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getPageImage</code>
     * method. Returns the image used to decorate the wizard page
     * 
     * @return byte[] the image used to decorate the wizard page
     */
    public byte[] getPageImage()
    {
        return Resources.getImage(Resources.PAGE_IMAGE);
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolName</code>
     * method. Returns the protocol name for this wizard.
     */
    public String getProtocolName()
    {
        return Resources.getString("protocolNameIcq");
    }

    /**
     * Implements the <code>AccountRegistrationWizard.getProtocolDescription
     * </code>
     * method. Returns the description of the protocol for this wizard.
     */
    public String getProtocolDescription()
    {
        return Resources.getString("protocolDescriptionIcq");
    }

    /**
     * Returns the set of pages contained in this wizard.
     */
    public Iterator getPages()
    {
        ArrayList pages = new ArrayList();
        firstWizardPage = new FirstWizardPage(this);

        pages.add(firstWizardPage);

        return pages.iterator();
    }

    /**
     * Returns the set of data that user has entered through this wizard.
     */
    public Iterator getSummary()
    {
        LinkedHashMap summaryTable = new LinkedHashMap();

        summaryTable.put(Resources.getString("uin"), registration.getUin());
        summaryTable.put(Resources.getString("rememberPassword"), new Boolean(registration
            .isRememberPassword()));

        if(registration.isAdvancedSettingsEnabled())
        {
            if (registration.getProxy() != null)
                summaryTable.put(Resources.getString("proxy"),
                    registration.getProxy());

            if (registration.getProxyPort() != null)
                summaryTable.put(Resources.getString("proxyPort"),
                    registration.getProxyPort());

            if (registration.getProxyType() != null)
                summaryTable.put(Resources.getString("proxyType"),
                    registration.getProxyType());

            if (registration.getProxyPort() != null)
                summaryTable.put(Resources.getString("proxyUsername"),
                    registration.getProxyUsername());

            if (registration.getProxyType() != null)
                summaryTable.put(Resources.getString("proxyPassword"),
                    registration.getProxyPassword());
        }

        return summaryTable.entrySet().iterator();
    }

    /**
     * Installs the account created through this wizard.
     * 
     * @return the <tt>ProtocolProviderService</tt> for the newly created
     * account.
     */
    public ProtocolProviderService signin()
    {
        if (!firstWizardPage.isCommitted())
            firstWizardPage.commitPage();

        return this.signin(registration.getUin(), registration
            .getPassword());
    }

    public ProtocolProviderService signin(String userName, String password)
    {
        firstWizardPage = null;
        ProtocolProviderFactory factory =
            IcqAccRegWizzActivator.getIcqProtocolProviderFactory();

        return this.installAccount(factory, userName, password);
    }

    /**
     * Creates an account for the given user and password.
     * 
     * @param providerFactory the ProtocolProviderFactory which will create the
     *            account
     * @param user the user identifier
     * @param passwd the password
     * @return the <tt>ProtocolProviderService</tt> for the new account.
     */
    public ProtocolProviderService installAccount(
        ProtocolProviderFactory providerFactory,
        String user,
        String passwd)
    {
        Hashtable accountProperties = new Hashtable();

        if (registration.isRememberPassword())
        {
            accountProperties.put(ProtocolProviderFactory.PASSWORD, passwd);
        }

        if (registration.isAdvancedSettingsEnabled())
        {
            if (registration.getProxy() != null)
                accountProperties.put(ProtocolProviderFactory.PROXY_ADDRESS,
                    registration.getProxy());

            if (registration.getProxyPort() != null)
                accountProperties.put(ProtocolProviderFactory.PROXY_PORT,
                    registration.getProxyPort());

            if (registration.getProxyType() != null)
                accountProperties.put(ProtocolProviderFactory.PROXY_TYPE,
                    registration.getProxyType());

            if (registration.getProxyUsername() != null)
                accountProperties.put(ProtocolProviderFactory.PROXY_USERNAME,
                    registration.getProxyUsername());

            if (registration.getProxyPassword() != null)
                accountProperties.put(ProtocolProviderFactory.PROXY_PASSWORD,
                    registration.getProxyPassword());
        }

        if (isModification)
        {
            providerFactory.modifyAccount(  protocolProvider,
                                            accountProperties);

            this.isModification  = false;

            return protocolProvider;
        }

        try
        {
            AccountID accountID =
                providerFactory.installAccount(user, accountProperties);

            ServiceReference serRef =
                providerFactory.getProviderForAccount(accountID);

            protocolProvider =
                (ProtocolProviderService) IcqAccRegWizzActivator.bundleContext
                    .getService(serRef);
        }
        catch (IllegalArgumentException e)
        {
            IcqAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(e.getMessage(),
                                        Resources.getString("error"),
                                        PopupDialog.ERROR_MESSAGE);
        }
        catch (IllegalStateException e)
        {
            IcqAccRegWizzActivator.getUIService().getPopupDialog()
                .showMessagePopupDialog(e.getMessage(),
                                        Resources.getString("error"),
                                        PopupDialog.ERROR_MESSAGE);
        }

        return protocolProvider;
    }

    /**
     * Fills the UIN and Password fields in this panel with the data coming
     * from the given protocolProvider.
     * 
     * @param protocolProvider The <tt>ProtocolProviderService</tt> to load
     *            the data from.
     */
    public void loadAccount(ProtocolProviderService protocolProvider)
    {
        this.isModification = true;

        this.protocolProvider = protocolProvider;

        this.registration = new IcqAccountRegistration();

        this.firstWizardPage.loadAccount(protocolProvider);
    }

    /**
     * Indicates if this wizard is opened for modification or for creating a
     * new account.
     * 
     * @return <code>true</code> if this wizard is opened for modification and
     * <code>false</code> otherwise.
     */
    public boolean isModification()
    {
        return isModification;
    }

    /**
     * Returns the wizard container, where all pages are added.
     * 
     * @return the wizard container, where all pages are added
     */
    public WizardContainer getWizardContainer()
    {
        return wizardContainer;
    }

    /**
     * Returns the registration object, which will store all the data through
     * the wizard.
     * 
     * @return the registration object, which will store all the data through
     * the wizard
     */
    public IcqAccountRegistration getRegistration()
    {
        return registration;
    }

    /**
     * Returns the size of this wizard.
     * @return the size of this wizard
     */
    public Dimension getSize()
    {
        return new Dimension(600, 500);
    }
    
    /**
     * Returns the identifier of the page to show first in the wizard.
     * @return the identifier of the page to show first in the wizard.
     */
    public Object getFirstPageIdentifier()
    {
        return firstWizardPage.getIdentifier();
    }

    /**
     * Returns the identifier of the page to show last in the wizard.
     * @return the identifier of the page to show last in the wizard.
     */
    public Object getLastPageIdentifier()
    {
        return firstWizardPage.getIdentifier();
    }

    /**
     * Sets the modification property to indicate if this wizard is opened for
     * a modification.
     * 
     * @param isModification indicates if this wizard is opened for modification
     * or for creating a new account. 
     */
    public void setModification(boolean isModification)
    {
        this.isModification = isModification;
    }

    /**
     * Returns an example string, which should indicate to the user how the
     * user name should look like.
     * @return an example string, which should indicate to the user how the
     * user name should look like.
     */
    public String getUserNameExample()
    {
        return FirstWizardPage.USER_NAME_EXAMPLE;
    }

    /**
     * Enables the simple "Sign in" form.
     */
    public boolean isSimpleFormEnabled()
    {
        return true;
    }

    /**
     * Opens the browser on the account registration page.
     */
    public void webSignup()
    {
        IcqAccRegWizzActivator.getBrowserLauncher().openURL(
            "https://www.icq.com/register/");
    }

    /**
     * Returns <code>true</code> if the web sign up is supported by the current
     * implementation, <code>false</code> - otherwise.
     * @return <code>true</code> if the web sign up is supported by the current
     * implementation, <code>false</code> - otherwise
     */
    public boolean isWebSignupSupported()
    {
        return true;
    }

    public Object getSimpleForm()
    {
        firstWizardPage = new FirstWizardPage(this);

        return firstWizardPage.getSimpleForm();
    }
}

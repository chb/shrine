package net.shrine.config;

import edu.harvard.i2b2.crc.datavo.i2b2message.PasswordType;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents the "minimal amount of information necessary" to obtain i2b2 configuration data from a Project Management Cell.
 * On SHRINE node startup, this class is used to find the SHERIFF_BROADCASTER and CRC.
 * This configuration file could also be used to find the ONT, for instance by the ShrimpETL util.
 * <p/>
 * The HiveCredentials serve dual purposes.
 * 1) Authorize a single shrine-application-user with the CRC
 * 2) Obtain a list of URLs for i2b2 components. It is unclear why authentication is required to list URLs. (Security by obscurity?)
 * <p/>
 * REFACTORED from 1.6.6
 * These details were contained in agent.xml, adapter.xml, ShrimpETlConfig.xml and potentially other locations throughout the 1.6.X releases.
 * For cleanliness, they were refactored to this single location instead.
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@XmlRootElement(name = "I2B2HiveCredentials")
@XmlType(propOrder = {"domain", "username", "password", "project"})
public class I2B2HiveCredentials
{
    /**
     * Credentials
     */
    protected String domain, username, project;
    protected PasswordType password;

    public I2B2HiveCredentials()
    {
        super();
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public PasswordType getPassword()
    {
        return password;
    }

    public void setPassword(PasswordType password)
    {
        this.password = password;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }

        I2B2HiveCredentials that = (I2B2HiveCredentials) o;

        if(domain != null ? !domain.equals(that.domain) : that.domain != null)
        {
            return false;
        }
        if(password != null ? !password.equals(that.password) : that.password != null)
        {
            return false;
        }
        if(project != null ? !project.equals(that.project) : that.project != null)
        {
            return false;
        }
        if(username != null ? !username.equals(that.username) : that.username != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = domain != null ? domain.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
